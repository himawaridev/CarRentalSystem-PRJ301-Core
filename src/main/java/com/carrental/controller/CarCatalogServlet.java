package com.carrental.controller;

import com.carrental.dao.CarDAO;
import com.carrental.model.Car;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/cars")
public class CarCatalogServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CarDAO carDAO = new CarDAO();
        List<Car> allCars = carDAO.getAllCars();

        String seatFilter = request.getParameter("seats");
        String brandFilter = normalize(request.getParameter("brand"));
        String statusFilter = normalize(request.getParameter("status"));
        String minPriceStr = normalize(request.getParameter("minPrice"));
        String maxPriceStr = normalize(request.getParameter("maxPrice"));

        Integer seatCount = parseInteger(seatFilter);
        BigDecimal minPrice = parseMoney(minPriceStr);
        BigDecimal maxPrice = parseMoney(maxPriceStr);

        boolean invalidFilter = (seatFilter != null && !seatFilter.isBlank() && seatCount == null)
                || (minPriceStr != null && !minPriceStr.isBlank() && minPrice == null)
                || (maxPriceStr != null && !maxPriceStr.isBlank() && maxPrice == null)
                || (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0)
                || (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0)
                || (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0);

        if (invalidFilter) {
            request.setAttribute("catalogError", "Bo loc khong hop le. Vui long kiem tra so cho va khoang gia.");
            minPrice = null;
            maxPrice = null;
        }

        BigDecimal minRate = minPrice;
        BigDecimal maxRate = maxPrice;
        List<Car> filtered = allCars.stream()
                .filter(c -> seatCount == null || c.getSeatCount() == seatCount)
                .filter(c -> brandFilter == null || brandFilter.isBlank()
                        || c.getBrand().equalsIgnoreCase(brandFilter))
                .filter(c -> statusFilter == null || statusFilter.isBlank()
                        || c.getStatus().equalsIgnoreCase(statusFilter))
                .filter(c -> minRate == null || c.getDailyRate().compareTo(minRate) >= 0)
                .filter(c -> maxRate == null || c.getDailyRate().compareTo(maxRate) <= 0)
                .toList();

        request.setAttribute("cars", filtered);
        request.setAttribute("allCars", allCars);
        request.setAttribute("brands", carDAO.getAvailableBrands());
        request.setAttribute("seatCounts", carDAO.getAvailableSeatCounts());
        request.setAttribute("seatFilter", seatFilter);
        request.setAttribute("brandFilter", brandFilter);
        request.setAttribute("statusFilter", statusFilter);
        request.setAttribute("minPrice", minPriceStr);
        request.setAttribute("maxPrice", maxPriceStr);

        request.getRequestDispatcher("/WEB-INF/views/car-catalog.jsp").forward(request, response);
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
