package github.tourism.web.filter;

import github.tourism.config.security.JwtTokenProvider;
import github.tourism.data.repository.user.RefreshRepository;
import github.tourism.web.advice.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshRepository refreshRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        String requestUri = request.getRequestURI();
        if (!requestUri.matches("^\\/logout$")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();
        if (!requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        String refresh = getRefreshTokenFromCookies(request);

        if (refresh == null) {
            createErrorResponse(response, ErrorCode.UNAUTHORIZED_REFRESH);
            return;
        }

        if (jwtTokenProvider.isExpired(refresh)) {
            createErrorResponse(response, ErrorCode.EXPIRED_REFRESH);
            return;
        }

        String category = jwtTokenProvider.getCategory(refresh);
        if (!"refresh".equals(category)) {
            createErrorResponse(response, ErrorCode.UNAUTHORIZED_REFRESH2);
            return;
        }

        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {
            createErrorResponse(response, ErrorCode.UNAUTHORIZED_REFRESH2);
            return;
        }

        refreshRepository.deleteByRefresh(refresh);

//        String domain = "https://seoultourismweb.vercel.app"; // 기본 도메인
//        if (request.getServerName().equals("localhost")) {
//            domain = "localhost"; // 로컬 환경에서는 도메인을 localhost로 설정 "http://localhost:3000"
//        }
//
//        Cookie cookie = new Cookie("refresh", null);
//        cookie.setMaxAge(0);
//        cookie.setPath("/");
//        cookie.setHttpOnly(true);
//        cookie.setDomain(domain); // ✅ 도메인 설정
//        cookie.setSecure(!"localhost".equals(domain)); // ✅ 로컬 환경에서는 Secure 비활성화
//        response.addCookie(cookie);
//        response.setStatus(HttpServletResponse.SC_OK);

        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        String origin = request.getHeader("Origin");
        if (origin != null && origin.contains("localhost")) {
            cookie.setSecure(false); // 로컬에서는 Secure 속성 제거
            cookie.setAttribute("SameSite", "Lax");
        } else {
            cookie.setSecure(true); // 배포 환경에서는 Secure 적용
            cookie.setAttribute("SameSite", "None");
            cookie.setDomain("seoultourismweb.vercel.app");
        }
        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204 No Content
    }

    private void createErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + errorCode.getErrorMessage() + "\"}");
    }

    private String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}