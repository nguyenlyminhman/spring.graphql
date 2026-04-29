package com.spring.graphql.security;

import com.spring.graphql.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService        jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                String username = jwtService.validateAndExtractUsername(token);

                // Chỉ set nếu SecurityContext chưa có authentication
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities() // rỗng - không dùng role
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    // ✅ Đặt vào SecurityContext → request này được xác thực
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("[JwtAuthFilter] Authenticated: {}", username);
                }

            } catch (ExpiredJwtException e) {
                // Token hết hạn - ghi log, tiếp tục như anonymous
                // Resolver có @RequireLogin sẽ throw UnauthorizedException
                log.warn("[JwtAuthFilter] Token hết hạn: {}", e.getMessage());

            } catch (JwtException e) {
                // Token không hợp lệ - bỏ qua
                log.warn("[JwtAuthFilter] Token không hợp lệ: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
