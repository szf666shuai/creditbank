package com.creditbank.platform;

import com.creditbank.platform.config.AuthInterceptor;
import com.creditbank.platform.context.UserContext;
import com.creditbank.platform.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthInterceptorTests {

    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final AuthInterceptor authInterceptor = new AuthInterceptor(jwtUtil);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void learningResourcesAllowsGuests() {
        HttpServletRequest request = learningResourcesRequest(null);

        assertTrue(authInterceptor.preHandle(request, response, new Object()));
        assertNull(UserContext.getUserId());
    }

    @Test
    void learningResourcesLoadsIdentityWhenTokenExists() {
        HttpServletRequest request = learningResourcesRequest("Bearer valid-token");
        when(jwtUtil.getUserId("valid-token")).thenReturn(8L);

        assertTrue(authInterceptor.preHandle(request, response, new Object()));
        assertEquals(8L, UserContext.getUserId());
    }

    private HttpServletRequest learningResourcesRequest(String authorization) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/learning/resources");
        when(request.getHeader("Authorization")).thenReturn(authorization);
        return request;
    }
}
