package com.carrental.controller;

import com.carrental.dao.CarDAO;
import com.carrental.model.Car;
import com.carrental.model.CarGroup;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/search")
public class CarSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forwardSearch(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String seatCountStr = request.getParameter("seatCount");
        String brandFilter = normalize(request.getParameter("brand"));
        String minPriceStr = normalize(request.getParameter("minPrice"));
        String maxPriceStr = normalize(request.getParameter("maxPrice"));
        String pickupStr = request.getParameter("pickupAt");
        String returnStr = request.getParameter("returnAt");

        request.setAttribute("seatCount", seatCountStr);
        request.setAttribute("brandFilter", brandFilter);
        request.setAttribute("minPrice", minPriceStr);
        request.setAttribute("maxPrice", maxPriceStr);
        request.setAttribute("pickupAt", pickupStr);
        request.setAttribute("returnAt", returnStr);

        Integer seatCount = parseInteger(seatCountStr);
        BigDecimal minPrice = parseMoney(minPriceStr);
        BigDecimal maxPrice = parseMoney(maxPriceStr);

        if ((seatCountStr != null && !seatCountStr.isBlank() && seatCount == null)
                || (minPriceStr != null && !minPriceStr.isBlank() && minPrice == null)
                || (maxPriceStr != null && !maxPriceStr.isBlank() && maxPrice == null)) {
            request.setAttribute("error", "Bo loc khong hop le. Vui long kiem tra so cho va muc gia.");
            forwardSearch(request, response);
            return;
        }

        if ((minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0)
                || (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0)
                || (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0)) {
            request.setAttribute("error", "Khoang gia khong hop le. Gia toi thieu phai nho hon hoac bang gia toi da.");
            forwardSearch(request, response);
            return;
        }

        LocalDateTime pickupAt;
        LocalDateTime returnAt;
        try {
            pickupAt = LocalDateTime.parse(pickupStr);
            returnAt = LocalDateTime.parse(returnStr);
        } catch (DateTimeParseException | NullPointerException e) {
            request.setAttribute("error", "Vui long chon day du ngay gio nhan va tra xe.");
            forwardSearch(request, response);
            return;
        }

        if (!returnAt.isAfter(pickupAt)) {
            request.setAttribute("error", "Ngay tra xe phai sau ngay nhan xe!");
            forwardSearch(request, response);
            return;
        }

        CarDAO carDAO = new CarDAO();
        List<CarGroup> carGroups = carDAO.findAvailableCarGroups(
                seatCount,
                brandFilter,
                minPrice,
                maxPrice,
                pickupAt,
                returnAt);

        request.setAttribute("carGroups", carGroups);
        forwardSearch(request, response);
    }

    private void forwardSearch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CarDAO carDAO = new CarDAO();
        List<Car> featuredCars = carDAO.getCatalogCarGroups();
        request.setAttribute("brands", carDAO.getAvailableBrands());
        request.setAttribute("seatCounts", carDAO.getAvailableSeatCounts());
        request.setAttribute("featuredBrandGroups", groupCarsByBrand(featuredCars));
        request.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(request, response);
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseMoney(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private Map<String, List<Car>> groupCarsByBrand(List<Car> cars) {
        Map<String, List<Car>> groups = new LinkedHashMap<>();
        for (Car car : cars) {
            String brand = normalize(car.getBrand());
            if (brand == null || brand.isBlank()) {
                brand = "Khac";
            }
            groups.computeIfAbsent(brand, key -> new java.util.ArrayList<>()).add(car);
        }
        return groups;
    }
}
