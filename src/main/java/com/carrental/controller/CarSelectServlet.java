package com.carrental.controller;

import com.carrental.dao.CarDAO;
import com.carrental.model.Car;
import com.carrental.service.RentalPeriodValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/select-car")
public class CarSelectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String brand = normalize(request.getParameter("brand"));
        String model = normalize(request.getParameter("model"));
        String pickupStr = request.getParameter("pickupAt");
        String returnStr = request.getParameter("returnAt");
        String seatCountStr = request.getParameter("seatCount");
        String minPriceStr = normalize(request.getParameter("minPrice"));
        String maxPriceStr = normalize(request.getParameter("maxPrice"));

        request.setAttribute("brand", brand);
        request.setAttribute("model", model);
        request.setAttribute("pickupAt", pickupStr);
        request.setAttribute("returnAt", returnStr);
        request.setAttribute("seatCount", seatCountStr);
        request.setAttribute("minPrice", minPriceStr);
        request.setAttribute("maxPrice", maxPriceStr);

        Integer seatCount = parseInteger(seatCountStr);
        BigDecimal minPrice = parseMoney(minPriceStr);
        BigDecimal maxPrice = parseMoney(maxPriceStr);

        if (brand == null || brand.isBlank() || model == null || model.isBlank()) {
            request.setAttribute("error", "Thieu thong tin hang xe hoac dong xe.");
            forward(request, response);
            return;
        }

        if ((seatCountStr != null && !seatCountStr.isBlank() && seatCount == null)
                || (minPriceStr != null && !minPriceStr.isBlank() && minPrice == null)
                || (maxPriceStr != null && !maxPriceStr.isBlank() && maxPrice == null)) {
            request.setAttribute("error", "Bo loc khong hop le. Vui long quay lai tim kiem.");
            forward(request, response);
            return;
        }

        LocalDateTime pickupAt;
        LocalDateTime returnAt;
        try {
            pickupAt = LocalDateTime.parse(pickupStr);
            returnAt = LocalDateTime.parse(returnStr);
        } catch (DateTimeParseException | NullPointerException e) {
            request.setAttribute("error", "Ngay gio nhan/tra xe khong hop le.");
            forward(request, response);
            return;
        }

        if (RentalPeriodValidator.isPickupInPast(pickupAt)) {
            request.setAttribute("error", "Thoi gian nhan xe khong duoc nam trong qua khu.");
            forward(request, response);
            return;
        }

        if (!returnAt.isAfter(pickupAt)) {
            request.setAttribute("error", "Ngay tra xe phai sau ngay nhan xe.");
            forward(request, response);
            return;
        }

        CarDAO carDAO = new CarDAO();
        List<Car> cars = carDAO.findSpecificAvailableCarsByGroup(
                brand, model, seatCount, minPrice, maxPrice, pickupAt, returnAt);
        request.setAttribute("cars", cars);
        forward(request, response);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/select-car.jsp").forward(request, response);
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
}
