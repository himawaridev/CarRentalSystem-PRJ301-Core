package com.carrental.controller;

import com.carrental.dao.AuthDAO;
import com.carrental.model.User;
import com.carrental.service.OAuthProfile;
import com.carrental.service.OAuthService;
import com.carrental.service.PasswordHasher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(urlPatterns = {
        "/oauth/google",
        "/oauth/facebook",
        "/oauth/google/callback",
        "/oauth/facebook/callback"
})
public class OAuthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String provider = providerFromPath(request.getServletPath());
        if (provider == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=oauth");
            return;
        }

        if (request.getServletPath().endsWith("/callback")) {
            handleCallback(request, response, provider);
        } else {
            startLogin(request, response, provider);
        }
    }

    private void startLogin(HttpServletRequest request, HttpServletResponse response, String provider)
            throws IOException {
        OAuthService oauth = new OAuthService();
        if (!oauth.isConfigured(provider)) {
            request.getSession().setAttribute("flashError",
                    "Chua cau hinh dang nhap " + provider + ".");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String state = PasswordHasher.randomUrlToken();
        request.getSession().setAttribute("oauthState:" + provider, state);
        response.sendRedirect(oauth.buildAuthorizationUrl(provider, state));
    }

    private void handleCallback(
            HttpServletRequest request,
            HttpServletResponse response,
            String provider) throws IOException {
        HttpSession session = request.getSession();
        String expectedState = (String) session.getAttribute("oauthState:" + provider);
        session.removeAttribute("oauthState:" + provider);
        String actualState = request.getParameter("state");
        if (expectedState == null || !expectedState.equals(actualState)) {
            session.setAttribute("flashError", "Phien dang nhap OAuth khong hop le. Hay thu lai.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String code = request.getParameter("code");
        if (code == null || code.isBlank()) {
            session.setAttribute("flashError", "Khong nhan duoc ma xac thuc OAuth.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            OAuthProfile profile = new OAuthService().fetchProfile(provider, code);
            User user = new AuthDAO().loginOrCreateOAuthUser(profile);
            if (user == null) {
                session.setAttribute("flashError",
                        "Khong the dang nhap OAuth. Tai khoan can co email da xac minh.");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            AuthSessionHelper.signIn(request, response, user, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            session.setAttribute("flashError", "Dang nhap OAuth bi gian doan. Hay thu lai.");
            response.sendRedirect(request.getContextPath() + "/login");
        } catch (Exception e) {
            session.setAttribute("flashError", "Dang nhap OAuth that bai: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }

    private String providerFromPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.contains("/google")) {
            return OAuthService.GOOGLE;
        }
        if (path.contains("/facebook")) {
            return OAuthService.FACEBOOK;
        }
        return null;
    }
}
