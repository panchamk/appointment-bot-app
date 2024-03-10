package com.pkg.appointmentscheduler.module.rest.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class PageRefreshFilter implements Filter {
    private static final String PAGE = "/pagerefresh/pagerefresh.html";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String path = request.getServletPath();
        if ("/".equals(path) || path.contains(".") || path.contains("v1/appointment") || path.contains("v1/doctor")) {
            chain.doFilter(request, response);
        } else {
            request.getRequestDispatcher(PAGE).forward(request, response);
        }
    }
}
