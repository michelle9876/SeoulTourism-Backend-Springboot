package github.tourism.service.user;

import github.tourism.config.security.JwtTokenProvider;
import github.tourism.data.entity.user.RefreshToken;
import github.tourism.data.repository.user.RefreshRepository;
import github.tourism.service.user.security.CustomUserDetails;
import github.tourism.web.advice.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshRepository refreshRepository;
    private final UserDetailsService userDetailsService;

    public Map<String, String> reissueTokens(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String refresh = getRefreshTokenFromCookies(request);

        log.info(refresh);
        if (refresh == null) {
            throw new Exception(ErrorCode.UNAUTHORIZED_REFRESH.getErrorMessage());
        }

        if (jwtTokenProvider.isExpired(refresh)) {
            throw new Exception(ErrorCode.EXPIRED_REFRESH.getErrorMessage());
        }

        String category = jwtTokenProvider.getCategory(refresh);
        if (!"refresh".equals(category)) {
            throw new Exception(ErrorCode.UNAUTHORIZED_REFRESH2.getErrorMessage());
        }

        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {
            throw new Exception(ErrorCode.UNAUTHORIZED_REFRESH2.getErrorMessage());
        }

        String email = jwtTokenProvider.getUserEmail(refresh);
        List<String> roles = jwtTokenProvider.getRoles(refresh);
        Long expiredMs = jwtTokenProvider.getExpirationTime(refresh);

        CustomUserDetails userPrincipal = (CustomUserDetails) userDetailsService.loadUserByUsername(email);
        String username = userPrincipal.getUserName();
        Integer userId = userPrincipal.getUserId();

        String newAccess = jwtTokenProvider.createToken("access", userId, email, username, roles);
        String newRefresh = jwtTokenProvider.createRefreshToken("refresh", email, roles);

        refreshRepository.deleteByRefresh(refresh);
        addRefreshEntity(email, newRefresh, expiredMs);

        response.setHeader("Authorization", "Bearer " + newAccess);
        Cookie refreshCookie = createCookie("refresh", newRefresh,request);
        response.addCookie(refreshCookie);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("Authorization", "Bearer " + newAccess);
        tokens.put("refresh", newRefresh);
        return tokens;
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

    private void addRefreshEntity(String email, String refresh, Long expiredMs) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiredMs);

        RefreshToken refreshEntity = new RefreshToken();
        refreshEntity.setEmail(email);
        refreshEntity.setRefresh(refresh);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        String formattedExpirationDate = sdf.format(expirationDate);
        refreshEntity.setExpiration(formattedExpirationDate);

        refreshRepository.save(refreshEntity);
    }

    private Cookie createCookie(String key, String value, HttpServletRequest request) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(3 * 60 * 60); // 쿠키의 유효 기간을 설정
        cookie.setPath("/"); // 쿠키가 유효한 경로를 설정
        cookie.setHttpOnly(true); // 쿠키를 HTTP 전용으로 설정
        cookie.setAttribute("SameSite", "None");
        String domain = "seoultourism.store"; //"seoultourismweb.vercel.app";
        if (request.getServerName().equals("localhost")) {
            domain = "localhost";
            cookie.setSecure(false);
            cookie.setAttribute("SameSite", "Lax");
        }
        cookie.setSecure(!"localhost".equals(domain));

        return cookie;
    }
}
