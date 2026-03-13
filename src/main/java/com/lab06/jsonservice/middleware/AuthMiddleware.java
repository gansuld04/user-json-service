package com.lab06.jsonservice.middleware;

import com.lab06.jsonservice.soap.SoapClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ================================================================
 * AuthMiddleware - JWT/Token validation middleware
 * ================================================================
 * Архитектур:
 *   Frontend -> JSON Service -> [AuthMiddleware] -> SOAP ValidateToken
 *                                    |
 *                              valid=true  -> request үргэлжлэнэ
 *                              valid=false -> 401 Unauthorized буцаана
 *
 * OncePerRequestFilter: Нэг request-т яг нэг удаа ажиллана
 *
 * Header формат:
 *   Authorization: Bearer <token>
 * ================================================================
 */
@Component
public class AuthMiddleware extends OncePerRequestFilter {

    @Autowired
    private SoapClient soapClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // ============================================================
        // CORS Preflight request-г шууд зөвшөөрөх
        // Browser OPTIONS request илгээж CORS шалгадаг
        // ============================================================
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // ============================================================
        // H2 Console, health check зэрэг замуудыг хамгаалахгүй
        // ============================================================
        if (path.startsWith("/h2-console") || path.equals("/health")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ============================================================
        // /users/** замд бүх request-т token шалгана
        // ============================================================
        // POST /users = профайл үүсгэх (register flow) -> token шаардахгүй
        // Бусад бүх /users/** -> token шаардана
        if (path.startsWith("/users") && !(path.equals("/users") && "POST".equalsIgnoreCase(method))) {

            // Authorization header авах
            String authHeader = request.getHeader("Authorization");

            // Header байхгүй эсвэл "Bearer " prefix байхгүй бол 401
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendUnauthorized(response, "Missing or invalid Authorization header");
                return;
            }

            // "Bearer " prefix-г хасаж token авах
            String token = authHeader.substring(7);

            // SOAP Service руу ValidateToken дуудах
            SoapClient.ValidateResult result = soapClient.validateToken(token);

            if (!result.valid) {
                // Token хүчингүй эсвэл дууссан
                sendUnauthorized(response, "Invalid or expired token");
                return;
            }

            // Token хүчинтэй - хэрэглэгчийн мэдээллийг request attribute-д хийнэ
            // Controller-т userId, username-г ашиглах боломжтой болно
            request.setAttribute("userId", result.userId);
            request.setAttribute("username", result.username);

            System.out.println("[AuthMiddleware] Authorized: " + result.username
                               + " (userId=" + result.userId + ") -> " + method + " " + path);
        }

        // Middleware дамжиж дараагийн filter/controller руу үргэлжлэнэ
        filterChain.doFilter(request, response);
    }

    /**
     * 401 Unauthorized хариу явуулах helper
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json");
        response.getWriter().write(
            "{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}"
        );
    }
}