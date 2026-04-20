//     package com.hitster.config;
//
//     import com.hitster.service.DatabaseService;
//     import jakarta.servlet.http.HttpServletRequest;
//     import jakarta.servlet.http.HttpServletResponse;
//     import org.springframework.lang.NonNull;
//     import org.springframework.stereotype.Component;
//     import org.springframework.web.servlet.HandlerInterceptor;
//
//     @Component
//     public class AuthInterceptor implements HandlerInterceptor {
//
//          @Override
//          public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
//                    @NonNull Object handler) throws Exception {
//
//               String authHeader = request.getHeader("Authorization");
//
//               if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    response.getWriter().write("Missing or invalid Authorization header");
//                    return false;
//               }
//
//               String token = authHeader.substring(7);
//
//               // 1. Ask the database WHO owns this token
//               int userId = DatabaseService.getUserIdByToken(token);
//
//               // 2. If it returns -1, kick them out
//               if (userId == -1) {
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    response.getWriter().write("Invalid or expired token");
//                    return false;
//               }
//
//               // 3. THE MAGIC TRICK: Attach the user ID to the request!
//               request.setAttribute("authenticatedUserId", userId);
//
//               return true; // Let them through
//          }
//     }