package com.carrental.controller;

import com.carrental.dao.CarDAO;
import com.carrental.model.Car;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/search")
public class CarSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String seatCountStr = request.getParameter("seatCount");
        String pickupStr = request.getParameter("pickupAt");
        String returnStr = request.getParameter("returnAt");

        Integer seatCount = null;
        if (seatCountStr != null && !seatCountStr.isEmpty()) {
            seatCount = Integer.parseInt(seatCountStr);
        }

        LocalDateTime pickupAt = LocalDateTime.parse(pickupStr);
        LocalDateTime returnAt = LocalDateTime.parse(returnStr);

        if (!returnAt.isAfter(pickupAt)) {
            request.setAttribute("error", "Ngày trả xe phải sau ngày nhận xe!");
            request.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(request, response);
            return;
        }

        CarDAO carDAO = new CarDAO();
        List<Car> cars = carDAO.findAvailableCars(seatCount, pickupAt, returnAt);

        request.setAttribute("cars", cars);
        request.setAttribute("seatCount", seatCountStr);
        request.setAttribute("pickupAt", pickupStr);
        request.setAttribute("returnAt", returnStr);

        request.getRequestDispatcher("/WEB-INF/views/search.jsp").forward(request, response);
    }
}
