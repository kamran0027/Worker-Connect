package com.workerconnect.ratelimiter.filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.workerconnect.ratelimiter.service.RateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class RateLimitFilter  extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String uri = request.getRequestURI();
        return uri.startsWith("/css/")
            || uri.startsWith("/js/")
            || uri.startsWith("/images/")
            || uri.startsWith("/uploads/")
            || uri.startsWith("/webjars/");
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        
        String uri=request.getRequestURI();
        
        String key;
        int capacity;
        int refill;
         /*
         * LOGIN
         */

        if (isLoginRequest(request)) {

            key = "LOGIN:" + getClientIp(request);

            capacity = 10;

            refill = 5;

        }

        /*
         * PUBLIC
         */

        else if (isPublicEndpoint(uri)) {

            key = "PUBLIC:" + getClientIp(request);

            capacity = 50;

            refill = 5;

        }

        /*
         * AUTHENTICATED
         */

        else {

            Authentication auth =
                    SecurityContextHolder.getContext().getAuthentication();
            if(auth == null || !auth.isAuthenticated()) {
                filterChain.doFilter(request, response);
                return;
            }
            String username = auth.getName();

            key = "USER:" + username;

            capacity = 20;

            refill = 3;

        }
        boolean allowed = rateLimiterService.allowRequest(key, capacity, refill);
        System.out.println("Key = " + key);
        System.out.println("Capacity = " + capacity);
        System.out.println("Refill = " + refill);
        System.out.println("Allowed = " + allowed);

        if (!allowed) {

            request.setAttribute("message",
            "Too many requests. Please try again later.");
            response.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);

            request.getRequestDispatcher("/error/429")
                    .forward(request, response);

            return;

        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {

        return request.getMethod().equals("POST")
                &&
                request.getServletPath().equals("/auth/login");

    }
    
    private boolean isPublicEndpoint(String uri) {

        return uri.equals("/")
                || uri.equals("/home")
                || uri.equals("/about")
                || uri.equals("/contact")
                || uri.startsWith("/auth")
                || uri.startsWith("/workers/search")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/uploads/");

    }

    private String getClientIp(HttpServletRequest request) {
        System.out.println("***************************************************************");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Remote Address: " + request.getRemoteAddr());
        System.out.println("X-Forwarded-For: " + request.getHeader("X-Forwarded-For"));
        System.out.println("***************************************************************");

        String header =
                request.getHeader("X-Forwarded-For");

        if (header == null || header.isBlank()) {

            return request.getRemoteAddr();

        }

        return header.split(",")[0];

    }

}
