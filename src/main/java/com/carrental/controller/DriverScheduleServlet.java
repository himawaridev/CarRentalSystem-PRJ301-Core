package com.carrental.controller;

import com.carrental.dao.DriverDAO;
import com.carrental.dao.UserDAO;
import com.carrental.model.DriverAssignment;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/driver/schedule")
public class DriverScheduleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");

        UserDAO userDAO = new UserDAO();
        Integer driverId = userDAO.getDriverIdByUserId(user.getUserId());

        if (driverId != null) {
            DriverDAO driverDAO = new DriverDAO();
            List<DriverAssignment> schedule = driverDAO.getScheduleByDriverId(driverId);
            request.setAttribute("schedule", schedule);
        }

        String flash = (String) session.getAttribute("flashSuccess");
        if (flash != null) { request.setAttribute("success", flash); session.removeAttribute("flashSuccess"); }

        request.getRequestDispatcher("/WEB-INF/views/driver-schedule.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        long assignmentId = Long.parseLong(request.getParameter("assignmentId"));
        String newStatus = request.getParameter("status");

        DriverDAO driverDAO = new DriverDAO();
        boolean ok = driverDAO.updateAssignmentStatus(assignmentId, newStatus);
        session.setAttribute("flashSuccess", ok ? "Cập nhật trạng thái thành công!" : "Cập nhật thất bại!");

        response.sendRedirect(request.getContextPath() + "/driver/schedule");
    }
}
