package com.proyecto.academy_service.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtValidationFilterTest {

    private JwtValidationFilter filter;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    private String validToken;
    private String secretKeyBase64;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtValidationFilter();

        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        secretKeyBase64 = Encoders.BASE64.encode(key.getEncoded());

        ReflectionTestUtils.setField(filter, "secretKey", secretKeyBase64);
        ReflectionTestUtils.setField(filter, "jwtCookieName", "jwt-cookie");

        validToken = Jwts.builder()
                .setSubject("admin@colegioohiggins.cl")
                .claim("roles", List.of("ROLE_ADMIN"))
                .claim("userId", 1L)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @Test
    void doFilterInternal_cuandoTokenValidoEnCookie_configuraAutenticacion() throws Exception {
        Cookie cookie = new Cookie("jwt-cookie", validToken);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("admin@colegioohiggins.cl");
        assertThat(auth.getCredentials()).isEqualTo(1L);
        assertThat(auth.getAuthorities()).extracting("authority").contains("ROLE_ADMIN");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_cuandoTokenValidoEnHeader_configuraAutenticacion() throws Exception {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("admin@colegioohiggins.cl");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_cuandoNoHayToken_noConfiguraAutenticacion() throws Exception {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_cuandoTokenInvalido_noConfiguraAutenticacion() throws Exception {
        Cookie cookie = new Cookie("jwt-cookie", "token-invalido");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();

        verify(filterChain).doFilter(request, response);
    }
}
