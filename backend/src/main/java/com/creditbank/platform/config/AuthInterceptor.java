package com.creditbank.platform.config;

import com.creditbank.platform.common.BusinessException;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final Set<String> OPTIONAL_AUTH_PATHS = Set.of(
            "/api/agent/chat"
    );

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        boolean optional = OPTIONAL_AUTH_PATHS.contains(path);

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (optional) {
                return true;
            }
            throw new BusinessException(401, "未登录或登录已过期");
        }

        String token = authHeader.substring(7);
        try {
            Long userId = jwtUtil.getUserId(token);
            UserContext.setUserId(userId);
            return true;
        } catch (Exception e) {
            if (optional) {
                return true;
            }
            throw new BusinessException(401, "未登录或登录已过期");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
