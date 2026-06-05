package com.carrental.filter;

import jakarta.servlet.*;
import java.io.IOException;

/**
 * Ensures UTF-8 encoding for all requests and responses.
 */
public class CharacterEncodingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
