package com.carrental.controller;

import com.carrental.dao.CarDAO;
import com.carrental.dao.ContractDAO;
import com.carrental.dao.UserDAO;
import com.carrental.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/book")
public class BookingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pickupStr = request.getParameter("pickupAt");
        String returnStr = request.getParameter("returnAt");
        String[] carIds = request.getParameterValues("carId");

        if (pickupStr == null || returnStr == null || carIds == null || carIds.length == 0) {
            response.sendRedirect(request.getContextPath() + "/search");
            return;
        }

        LocalDateTime pickupAt = LocalDateTime.parse(pickupStr);
        LocalDateTime returnAt = LocalDateTime.parse(returnStr);
        long hours = ChronoUnit.HOURS.between(pickupAt, returnAt);
        BigDecimal days = BigDecimal.valueOf(Math.max(hours, 24))
                .divide(BigDecimal.valueOf(24), 2, RoundingMode.CEILING);

        CarDAO carDAO = new CarDAO();
        List<Car> selectedCars = new ArrayList<>();
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalRental = BigDecimal.ZERO;

        for (String idStr : carIds) {
            Car car = carDAO.getCarById(Integer.parseInt(idStr));
            if (car != null) {
                selectedCars.add(car);
                totalDeposit = totalDeposit.add(car.getDepositAmount());
                totalRental = totalRental.add(car.getDailyRate().multiply(days));
            }
        }

        request.setAttribute("selectedCars", selectedCars);
        request.setAttribute("pickupAt", pickupStr);
        request.setAttribute("returnAt", returnStr);
        request.setAttribute("days", days);
        request.setAttribute("totalDeposit", totalDeposit);
        request.setAttribute("totalRental", totalRental);

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
        Integer customerId = userDAO.getCustomerIdByUserId(user.getUserId());
        if (customerId == null) {
            request.setAttribute("error", "Tài khoản chưa có hồ sơ khách hàng!");
            request.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(request, response);
            return;
        }

        String pickupStr = request.getParameter("pickupAt");
        String returnStr = request.getParameter("returnAt");
        String pickupLocation = request.getParameter("pickupLocation");
        String returnLocation = request.getParameter("returnLocation");
        String[] carIds = request.getParameterValues("carId");
        String[] requiresDriverFlags = request.getParameterValues("requiresDriver");

        LocalDateTime pickupAt = LocalDateTime.parse(pickupStr);
        LocalDateTime returnAt = LocalDateTime.parse(returnStr);
        long hours = ChronoUnit.HOURS.between(pickupAt, returnAt);
        BigDecimal days = BigDecimal.valueOf(Math.max(hours, 24))
                .divide(BigDecimal.valueOf(24), 2, RoundingMode.CEILING);

        CarDAO carDAO = new CarDAO();
        List<ContractDetail> details = new ArrayList<>();
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < carIds.length; i++) {
            Car car = carDAO.getCarById(Integer.parseInt(carIds[i]));
            if (car == null) continue;

            boolean needsDriver = false;
            if (requiresDriverFlags != null) {
                for (String flag : requiresDriverFlags) {
                    if (flag.equals(carIds[i])) { needsDriver = true; break; }
                }
            }

            ContractDetail d = new ContractDetail();
            d.setCarId(car.getCarId());
            d.setRequiresDriver(needsDriver);
            d.setRentalDailyRate(car.getDailyRate());
            d.setDriverDailyRate(needsDriver ? new BigDecimal("300000") : BigDecimal.ZERO);
            d.setEstimatedDays(days);
            BigDecimal rentalAmount = car.getDailyRate().multiply(days);
            BigDecimal driverAmount = d.getDriverDailyRate().multiply(days);
            d.setRentalAmount(rentalAmount);
            d.setDriverAmount(driverAmount);

            details.add(d);
            totalDeposit = totalDeposit.add(car.getDepositAmount());
            totalAmount = totalAmount.add(rentalAmount).add(driverAmount);
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
        if (contractDAO.createContractWithDetails(contract, details)) {
            session.setAttribute("flashSuccess", "Đặt xe thành công! Hợp đồng đang chờ duyệt.");
            response.sendRedirect(request.getContextPath() + "/my-contracts");
        } else {
            request.setAttribute("error", "Đặt xe thất bại. Xe có thể đã được đặt bởi người khác.");
            request.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(request, response);
        }
    }
}
