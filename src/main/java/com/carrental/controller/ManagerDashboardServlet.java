package com.carrental.controller;

import com.carrental.dao.*;
import com.carrental.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/manager/dashboard")
public class ManagerDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String tab = request.getParameter("tab");
        if (tab == null) tab = "cars";

        CarDAO carDAO = new CarDAO();
        request.setAttribute("cars", carDAO.getAllCars());
        request.setAttribute("carTypes", carDAO.getAllCarTypes());

        if ("drivers".equals(tab)) {
            DriverDAO driverDAO = new DriverDAO();
            request.setAttribute("drivers", driverDAO.getActiveDrivers());
        }
        if ("assign".equals(tab)) {
            String contractIdStr = request.getParameter("contractId");
            if (contractIdStr != null) {
                long cid = Long.parseLong(contractIdStr);
                ContractDAO contractDAO = new ContractDAO();
                request.setAttribute("contract", contractDAO.getContractById(cid));
                request.setAttribute("details", contractDAO.getDetailsByContractId(cid));
            }
            DriverDAO driverDAO = new DriverDAO();
            request.setAttribute("drivers", driverDAO.getActiveDrivers());
            ContractDAO contractDAO = new ContractDAO();
            request.setAttribute("acceptedContracts", contractDAO.getContractsByStatus("ACCEPTED"));
        }

        request.setAttribute("tab", tab);

        HttpSession session = request.getSession(false);
        if (session != null) {
            String flash = (String) session.getAttribute("flashSuccess");
            if (flash != null) { request.setAttribute("success", flash); session.removeAttribute("flashSuccess"); }
            String err = (String) session.getAttribute("flashError");
            if (err != null) { request.setAttribute("error", err); session.removeAttribute("flashError"); }
        }

        request.getRequestDispatcher("/WEB-INF/views/manager-dashboard.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        if ("addCar".equals(action) || "editCar".equals(action)) {
            Car car = new Car();
            if ("editCar".equals(action)) {
                car.setCarId(Integer.parseInt(request.getParameter("carId")));
            }
            car.setCarTypeId(Integer.parseInt(request.getParameter("carTypeId")));
            car.setLicensePlate(request.getParameter("licensePlate"));
            car.setBrand(request.getParameter("brand"));
            car.setModel(request.getParameter("model"));
            String yearStr = request.getParameter("manufactureYear");
            if (yearStr != null && !yearStr.isEmpty()) car.setManufactureYear(Short.parseShort(yearStr));
            car.setColor(request.getParameter("color"));
            car.setTransmission(request.getParameter("transmission"));
            car.setFuelType(request.getParameter("fuelType"));
            car.setMileage(Integer.parseInt(request.getParameter("mileage")));
            car.setDailyRate(new BigDecimal(request.getParameter("dailyRate")));
            car.setDepositAmount(new BigDecimal(request.getParameter("depositAmount")));
            car.setStatus(request.getParameter("status"));
            car.setImageUrl(request.getParameter("imageUrl"));
            car.setDescription(request.getParameter("description"));

            CarDAO carDAO = new CarDAO();
            boolean ok;
            if ("addCar".equals(action)) ok = carDAO.insertCar(car);
            else ok = carDAO.updateCar(car);

            session.setAttribute(ok ? "flashSuccess" : "flashError",
                ok ? "Lưu xe thành công!" : "Lưu xe thất bại!");
            response.sendRedirect(request.getContextPath() + "/manager/dashboard?tab=cars");
        } else if ("assignDriver".equals(action)) {
            long detailId = Long.parseLong(request.getParameter("contractDetailId"));
            int driverId = Integer.parseInt(request.getParameter("driverId"));
            User user = (User) session.getAttribute("loggedInUser");

            DriverDAO driverDAO = new DriverDAO();
            boolean ok = driverDAO.assignDriver(detailId, driverId, user.getUserId());
            session.setAttribute(ok ? "flashSuccess" : "flashError",
                ok ? "Gán tài xế thành công!" : "Gán tài xế thất bại! Có thể tài xế đã bị trùng lịch.");
            response.sendRedirect(request.getContextPath() + "/manager/dashboard?tab=assign");
        } else {
            response.sendRedirect(request.getContextPath() + "/manager/dashboard");
        }
    }
}
