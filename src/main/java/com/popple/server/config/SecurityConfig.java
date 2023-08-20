package com.popple.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.popple.server.common.filter.JwtExceptionFilter;
import com.popple.server.common.filter.JwtRequestFilter;
import com.popple.server.domain.user.jwt.TokenManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenManager tokenManager;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final ObjectMapper objectMapper;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter(tokenManager);
    }

    @Bean
    public JwtExceptionFilter jwtExceptionFilter() {
        return new JwtExceptionFilter(objectMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        String[] permitAllUrls = new String[]{
                "/api/auth/password",
                "/api/auth/validate-business-number",
                "/api/auth/verify-email",
                "/api/auth/signup",
                "/api/auth/signin",
                "/api/auth/check-duplication",
                "/api/auth/signup/seller",
                "/api/auth/reissue",
                "/api/auth/kakaologin",
                "/api/auth/regenerate-token"
        };

        httpSecurity.csrf().disable(); // CSRF 해제
        httpSecurity.headers().frameOptions().disable(); // iframe 거부
        httpSecurity.cors().configurationSource(getConfigurationSource()); // cors 재설정
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // JSessionId가 응답되면 세션 영역에서 사라짐 (Stateless)
        httpSecurity.formLogin().disable(); // form 로그인 해제 (UsernamePasswordAuthenticationFilter 비활성화)
        httpSecurity.httpBasic().disable(); // 로그인 인증창이 뜨지 않도록 비활성화
        httpSecurity.authorizeRequests()
                //TODO 추후 모든 기능 완성되면 로그인 필요 없는 API들만 permit 시켜주기 ( => JWT Filter에 적용한 URL과 동일 )
                .antMatchers(permitAllUrls).permitAll()
                .anyRequest().authenticated();
        httpSecurity.exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .accessDeniedHandler(jwtAccessDeniedHandler);
        httpSecurity.addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterBefore(jwtExceptionFilter(), jwtRequestFilter().getClass());

        return httpSecurity.build();

    }

    private CorsConfigurationSource getConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*"); // 모든 요청 허용, JS 요청 허용
        //TODO 추후 배포시 수정 필요
        configuration.addAllowedOriginPattern("*");
        configuration.setAllowCredentials(true); // 클라이언트에서 쿠키 요청하는 것을 허용
        configuration.addExposedHeader("Authorization"); // Authorization 헤더를 노출

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", configuration);

        return urlBasedCorsConfigurationSource;
    }

}
