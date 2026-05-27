package com.anax.devops.app.infrastructure.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class SecurityAuthFilter implements Filter {

    private final JwtService jwtService;
    private final SecurityProperties properties;

    public SecurityAuthFilter(JwtService jwtService, SecurityProperties properties) {
        this.jwtService = jwtService;
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if ("/DevOps".equals(httpRequest.getRequestURI())) {
            String apiKey = httpRequest.getHeader(properties.apiKeyHeader());
            String jwt = httpRequest.getHeader(properties.jwtHeader());

            if (apiKey == null || !properties.apiKey().equals(apiKey)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter().write("Unauthorized: Missing or invalid API Key");
                return;
            }

            if (jwt == null || !jwtService.validateToken(jwt)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.getWriter().write("Unauthorized: Missing, expired or invalid JWT Token");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}