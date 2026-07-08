package com.carrental.controller;

import com.carrental.dao.UserDAO;
import com.carrental.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedInUser") != null) {
            response.sendRedirect(request.getContextPath() + "/search");
            return;
        }
        attachFlash(request);
        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        UserDAO userDAO = new UserDAO();
        User user = userDAO.login(username, password);

        if (user == null) {
            request.setAttribute("error", "Sai ten dang nhap hoac mat khau.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        AuthSessionHelper.signIn(request, response, user, request.getParameter("redirect"));
    }

    private void attachFlash(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        String success = (String) session.getAttribute("flashSuccess");
        if (success != null) {
            request.setAttribute("success", success);
            session.removeAttribute("flashSuccess");
        }
        String error = (String) session.getAttribute("flashError");
        if (error != null) {
            request.setAttribute("error", error);
            session.removeAttribute("flashError");
        }
    }
}
