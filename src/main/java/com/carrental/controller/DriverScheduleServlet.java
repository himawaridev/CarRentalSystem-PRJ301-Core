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
        String error = (String) session.getAttribute("flashError");
        if (error != null) { request.setAttribute("error", error); session.removeAttribute("flashError"); }

        request.getRequestDispatcher("/WEB-INF/views/driver-schedule.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loggedInUser");
        Integer driverId = new UserDAO().getDriverIdByUserId(user.getUserId());
        long assignmentId;
        try {
            assignmentId = Long.parseLong(request.getParameter("assignmentId"));
        } catch (NumberFormatException | NullPointerException e) {
            session.setAttribute("flashError", "Thong tin lich lai khong hop le.");
            response.sendRedirect(request.getContextPath() + "/driver/schedule");
            return;
        }
        String newStatus = request.getParameter("status");

        DriverDAO driverDAO = new DriverDAO();
        boolean ok = driverId != null
                && driverDAO.updateAssignmentStatus(assignmentId, driverId, newStatus);
        session.setAttribute(ok ? "flashSuccess" : "flashError",
                ok ? "Cap nhat trang thai thanh cong!"
                   : "Khong the cap nhat. Lich khong thuoc tai xe hoac sai thu tu trang thai.");

        response.sendRedirect(request.getContextPath() + "/driver/schedule");
    }
}
