package com.carrental.controller;

import com.carrental.dao.CarDAO;
import com.carrental.dao.ContractDAO;
import com.carrental.dao.UserDAO;
import com.carrental.model.Car;
import com.carrental.model.Contract;
import com.carrental.model.ContractDetail;
import com.carrental.model.PaymentMode;
import com.carrental.model.PaymentTransaction;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/book")
public class BookingServlet extends HttpServlet {

    private static final BigDecimal DRIVER_DAILY_RATE = new BigDecimal("300000");
    private static final String SPECIFIC_SELECTION_MODE = "specific";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pickupStr = request.getParameter("pickupAt");
        String returnStr = request.getParameter("returnAt");
        String[] carIds = request.getParameterValues("carId");
        String selectionMode = request.getParameter("selectionMode");

        if (redirectToProfileIfMissingBank(request, response)) {
            return;
        }

        if (!populateBookingView(request, pickupStr, returnStr, carIds, null, selectionMode)) {
            response.sendRedirect(request.getContextPath() + "/search");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("loggedInUser") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth");
            return;
        }

        UserDAO userDAO = new UserDAO();
        User currentUser = userDAO.getUserById(user.getUserId());
        if (currentUser == null || !currentUser.hasRefundBankInfo()) {
            response.sendRedirect(profileRequiredUrl(request));
            return;
        }

        Integer customerId = userDAO.getCustomerIdByUserId(user.getUserId());
        if (customerId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            request.setAttribute("error", "Tai khoan chua co ho so khach hang.");
            request.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(request, response);
            return;
        }

        String pickupStr = request.getParameter("pickupAt");
        String returnStr = request.getParameter("returnAt");
        String pickupLocation = normalizeLocation(request.getParameter("pickupLocation"));
        String returnLocation = normalizeLocation(request.getParameter("returnLocation"));
        String[] carIds = request.getParameterValues("carId");
        String[] requiresDriverFlags = request.getParameterValues("requiresDriver");
        String selectionMode = request.getParameter("selectionMode");
        PaymentMode paymentMode = PaymentMode.fromRequest(request.getParameter("paymentMode"));

        request.setAttribute("pickupLocation", pickupLocation);
        request.setAttribute("returnLocation", returnLocation);
        request.setAttribute("paymentMode", paymentMode.name());
        request.setAttribute("selectionMode", selectionMode);

        if (isBlank(pickupLocation) || isBlank(returnLocation)) {
            rejectBadBooking(request, response, pickupStr, returnStr, carIds, requiresDriverFlags, selectionMode,
                    "Vui long nhap day du dia chi nhan xe va dia chi tra xe.");
            return;
        }

        LocalDateTime pickupAt;
        LocalDateTime returnAt;
        try {
            pickupAt = LocalDateTime.parse(pickupStr);
            returnAt = LocalDateTime.parse(returnStr);
        } catch (DateTimeParseException | NullPointerException e) {
            rejectBadBooking(request, response, pickupStr, returnStr, carIds, requiresDriverFlags, selectionMode,
                    "Thoi gian nhan/tra xe khong hop le.");
            return;
        }

        if (!returnAt.isAfter(pickupAt) || carIds == null || carIds.length == 0) {
            rejectBadBooking(request, response, pickupStr, returnStr, carIds, requiresDriverFlags, selectionMode,
                    "Vui long chon xe va thoi gian tra xe phai sau thoi gian nhan xe.");
            return;
        }

        long hours = ChronoUnit.HOURS.between(pickupAt, returnAt);
        BigDecimal days = BigDecimal.valueOf(Math.max(hours, 24))
                .divide(BigDecimal.valueOf(24), 2, RoundingMode.CEILING);

        CarDAO carDAO = new CarDAO();
        List<ContractDetail> details = new ArrayList<>();
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<String> driverCarIds = toSet(requiresDriverFlags);
        Set<Integer> assignedCarIds = new HashSet<>();

        for (String carIdValue : carIds) {
            int representativeCarId;
            try {
                representativeCarId = Integer.parseInt(carIdValue);
            } catch (NumberFormatException e) {
                continue;
            }

            Car car = useSpecificSelection(selectionMode)
                    ? carDAO.findAvailableCarById(representativeCarId, pickupAt, returnAt, assignedCarIds)
                    : carDAO.findRandomAvailableCarInSameGroup(
                            representativeCarId, pickupAt, returnAt, assignedCarIds);
            if (car == null) {
                continue;
            }

            boolean needsDriver = driverCarIds.contains(carIdValue);
            BigDecimal rentalAmount = car.getDailyRate().multiply(days);
            BigDecimal driverAmount = needsDriver ? DRIVER_DAILY_RATE.multiply(days) : BigDecimal.ZERO;

            ContractDetail detail = new ContractDetail();
            detail.setCarId(car.getCarId());
            detail.setRequiresDriver(needsDriver);
            detail.setRentalDailyRate(car.getDailyRate());
            detail.setDriverDailyRate(needsDriver ? DRIVER_DAILY_RATE : BigDecimal.ZERO);
            detail.setEstimatedDays(days);
            detail.setRentalAmount(rentalAmount);
            detail.setDriverAmount(driverAmount);

            details.add(detail);
            assignedCarIds.add(car.getCarId());
            totalDeposit = totalDeposit.add(car.getDepositAmount());
            totalAmount = totalAmount.add(rentalAmount).add(driverAmount);
        }

        if (details.isEmpty()) {
            rejectBadBooking(request, response, pickupStr, returnStr, carIds, requiresDriverFlags, selectionMode,
                    "Khong tim thay xe hop le de dat.");
            return;
        }

        Contract contract = new Contract();
        contract.setCustomerId(customerId);
        contract.setPickupAt(pickupAt);
        contract.setReturnAt(returnAt);
        contract.setPickupLocation(pickupLocation);
        contract.setReturnLocation(returnLocation);
        contract.setDepositAmountDue(totalDeposit);
        contract.setFinalAmountDue(totalAmount);

        ContractDAO contractDAO = new ContractDAO();
        PaymentTransaction paymentTransaction = contractDAO.createPendingBookingWithDetailsAndPayments(
                contract, details, paymentMode);

        if (paymentTransaction == null) {
            String error = contractDAO.getLastErrorMessage();
            if (error == null || error.isBlank()) {
                error = "Dat xe that bai. Xe co the da duoc dat boi nguoi khac.";
            }
            rejectBadBooking(request, response, pickupStr, returnStr, carIds, requiresDriverFlags, selectionMode,
                    error);
            return;
        }

        String ref = URLEncoder.encode(paymentTransaction.getProviderTransactionRef(), StandardCharsets.UTF_8);
        response.sendRedirect(request.getContextPath() + "/payment/pending?ref=" + ref);
    }

    private void rejectBadBooking(
            HttpServletRequest request,
            HttpServletResponse response,
            String pickupStr,
            String returnStr,
            String[] carIds,
            String[] requiresDriverFlags,
            String selectionMode,
            String message) throws ServletException, IOException {

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.setAttribute("error", message);
        populateBookingView(request, pickupStr, returnStr, carIds, requiresDriverFlags, selectionMode);
        request.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(request, response);
    }

    private boolean populateBookingView(
            HttpServletRequest request,
            String pickupStr,
            String returnStr,
            String[] carIds,
            String[] requiresDriverFlags,
            String selectionMode) {

        if (pickupStr == null || returnStr == null || carIds == null || carIds.length == 0) {
            return false;
        }

        try {
            LocalDateTime pickupAt = LocalDateTime.parse(pickupStr);
            LocalDateTime returnAt = LocalDateTime.parse(returnStr);
            if (!returnAt.isAfter(pickupAt)) {
                return false;
            }

            long hours = ChronoUnit.HOURS.between(pickupAt, returnAt);
            BigDecimal days = BigDecimal.valueOf(Math.max(hours, 24))
                    .divide(BigDecimal.valueOf(24), 2, RoundingMode.CEILING);

            CarDAO carDAO = new CarDAO();
            List<Car> selectedCars = new ArrayList<>();
            BigDecimal totalDeposit = BigDecimal.ZERO;
            BigDecimal totalRental = BigDecimal.ZERO;
            BigDecimal totalDriverFee = BigDecimal.ZERO;
            Set<String> driverCarIds = toSet(requiresDriverFlags);
            Set<Integer> selectedPhysicalCarIds = new HashSet<>();

            for (String idStr : carIds) {
                int representativeCarId = Integer.parseInt(idStr);
                Car car = useSpecificSelection(selectionMode)
                        ? carDAO.findAvailableCarById(
                                representativeCarId, pickupAt, returnAt, selectedPhysicalCarIds)
                        : carDAO.findRandomAvailableCarInSameGroup(
                                representativeCarId, pickupAt, returnAt, selectedPhysicalCarIds);
                if (car == null) {
                    continue;
                }
                selectedCars.add(car);
                selectedPhysicalCarIds.add(car.getCarId());
                totalDeposit = totalDeposit.add(car.getDepositAmount());
                totalRental = totalRental.add(car.getDailyRate().multiply(days));
                if (driverCarIds.contains(idStr)) {
                    totalDriverFee = totalDriverFee.add(DRIVER_DAILY_RATE.multiply(days));
                }
            }

            request.setAttribute("selectedCars", selectedCars);
            request.setAttribute("pickupAt", pickupStr);
            request.setAttribute("returnAt", returnStr);
            request.setAttribute("days", days);
            request.setAttribute("totalDeposit", totalDeposit);
            request.setAttribute("totalRental", totalRental);
            request.setAttribute("totalDriverFee", totalDriverFee);
            request.setAttribute("fullPrepaymentTotal", totalDeposit.add(totalRental).add(totalDriverFee));
            request.setAttribute("selectedDriverCarIds", driverCarIds);
            request.setAttribute("selectionMode", selectionMode);
            return !selectedCars.isEmpty();
        } catch (DateTimeParseException | NumberFormatException e) {
            return false;
        }
    }

    private String normalizeLocation(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean useSpecificSelection(String selectionMode) {
        return SPECIFIC_SELECTION_MODE.equalsIgnoreCase(selectionMode);
    }

    private Set<String> toSet(String[] values) {
        Set<String> set = new HashSet<>();
        if (values != null) {
            for (String value : values) {
                set.add(value);
            }
        }
        return set;
    }

    private boolean redirectToProfileIfMissingBank(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return false;
        }

        User currentUser = new UserDAO().getUserById(user.getUserId());
        if (currentUser != null && currentUser.hasRefundBankInfo()) {
            return false;
        }

        if (session != null) {
            session.setAttribute("flashError",
                    "Vui long cap nhat tai khoan ngan hang nhan hoan coc truoc khi dat xe.");
        }
        response.sendRedirect(profileRequiredUrl(request));
        return true;
    }

    private String profileRequiredUrl(HttpServletRequest request) {
        String current = request.getRequestURI();
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            current += "?" + request.getQueryString();
        }
        return request.getContextPath() + "/profile?required=bank&redirect="
                + URLEncoder.encode(current, StandardCharsets.UTF_8);
    }
}
