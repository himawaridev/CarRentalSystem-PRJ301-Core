package com.carrental.controller;

import com.carrental.dao.CarDAO;
import com.carrental.model.Car;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/cars")
public class CarCatalogServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CarDAO carDAO = new CarDAO();
        List<Car> allCars = carDAO.getAllCars();

        // Filter params
        String seatFilter = request.getParameter("seats");
        String brandFilter = request.getParameter("brand");
        String statusFilter = request.getParameter("status");

        // Apply filters
        List<Car> filtered = allCars.stream()
            .filter(c -> seatFilter == null || seatFilter.isEmpty()
                || String.valueOf(c.getSeatCount()).equals(seatFilter))
            .filter(c -> brandFilter == null || brandFilter.isEmpty()
                || c.getBrand().equalsIgnoreCase(brandFilter))
            .filter(c -> statusFilter == null || statusFilter.isEmpty()
                || c.getStatus().equalsIgnoreCase(statusFilter))
            .toList();

        // Get unique brands for filter dropdown
        List<String> brands = allCars.stream()
            .map(Car::getBrand)
            .distinct()
            .sorted()
            .toList();

        request.setAttribute("cars", filtered);
        request.setAttribute("allCars", allCars);
        request.setAttribute("brands", brands);
        request.setAttribute("seatFilter", seatFilter);
        request.setAttribute("brandFilter", brandFilter);
        request.setAttribute("statusFilter", statusFilter);

        request.getRequestDispatcher("/WEB-INF/views/car-catalog.jsp").forward(request, response);
    }
}
