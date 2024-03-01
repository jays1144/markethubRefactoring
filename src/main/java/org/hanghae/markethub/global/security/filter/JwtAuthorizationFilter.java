package org.hanghae.markethub.global.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hanghae.markethub.global.security.impl.UserDetailsServiceImpl;
import org.hanghae.markethub.global.security.jwt.JwtUtil;
import org.hanghae.markethub.global.security.service.SecurityRedisService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final SecurityRedisService securityRedisService;


    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {

        String refreshToken = jwtUtil.getTokenFromRequest(req);

        if (StringUtils.hasText(refreshToken)) {
            // JWT 토큰 substring
            refreshToken = jwtUtil.substringToken(refreshToken);
            log.info(refreshToken);

            if (!jwtUtil.validateToken(refreshToken)) {
//                log.error("Token Error");
                Cookie cookie = new Cookie("refreshToken", null);
                cookie.setMaxAge(0);
                res.addCookie(cookie);

                // Redis 데이터베이스에서 토큰 제거
                String refreshTokenKey = "refreshToken:" + refreshToken;
                securityRedisService.deleteValues(refreshTokenKey);
            }

            Claims info = jwtUtil.getUserInfoFromToken(refreshToken);

            try {
                setAuthentication(info.getSubject());
            } catch (Exception e) {
//                log.error(e.getMessage());
                return;
            }
        }

        filterChain.doFilter(req, res);
    }

    // 인증 처리
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}