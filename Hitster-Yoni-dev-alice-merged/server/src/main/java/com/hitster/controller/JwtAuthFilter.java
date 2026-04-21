package com.hitster.controller;

import com.hitster.service.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String servletPath = request.getServletPath();
        String contextPath = request.getContextPath();

        System.out.println("JwtAuthFilter.shouldNotFilter -> uri=" + uri
                + " | servletPath=" + servletPath
                + " | contextPath=" + contextPath);

        return uri.startsWith("/audio/")
                || uri.equals("/audio")
                || uri.startsWith("/error")
                || uri.equals("/favicon.ico")
                || uri.equals("/api/auth/login")
                || uri.equals("/api/auth/register")
                || uri.equals("/api/auth/forgot-password");
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        System.out.println("JwtAuthFilter.doFilterInternal -> uri=" + uri);

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            throw new JwtException("Missing token");
        }

        String token = header.substring(7).trim();

        if (token.isEmpty()) {
            throw new JwtException("Missing token");
        }

        Claims claims = JwtUtil.parseToken(token);

        request.setAttribute("jwtUserId", claims.get("userId"));
        request.setAttribute("jwtUsername", claims.get("username"));
        request.setAttribute("jwtIsAdmin", claims.get("isAdmin"));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        Boolean isAdmin = claims.get("isAdmin", Boolean.class);

        if (Boolean.TRUE.equals(isAdmin)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        claims.getSubject(),
                        null,
                        authorities
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}