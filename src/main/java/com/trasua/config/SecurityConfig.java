package com.trasua.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * PHÂN QUYỀN:
 *   - Khách vãng lai : xem menu, đăng ký, đăng nhập.
 *   - CUSTOMER       : đặt món, giỏ hàng, lịch sử đơn của mình, huỷ đơn chờ xác nhận.
 *   - STAFF          : + bán hàng tại quầy, xử lý đơn (/staff/**), chấm công & xem lương (/staff/timesheet).
 *   - ADMIN          : + toàn quyền quản trị (/admin/**): thống kê, menu,
 *                      topping, khuyến mãi, khách hàng, nhân viên, bảng lương, tài khoản.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ObjectProvider<ClientRegistrationRepository> googleClients,
                                                   GoogleLoginSuccessHandler googleSuccessHandler) throws Exception {
        // Đăng nhập bằng Google: chỉ bật khi đã cấu hình Client ID (xem GoogleLoginConfig)
        if (googleClients.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                    .loginPage("/login")
                    .successHandler(googleSuccessHandler)
                    .failureUrl("/login?error=google"));
        }
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico").permitAll()
                        .requestMatchers("/", "/menu-board", "/products/**", "/api/**").permitAll()
                        .requestMatchers("/login", "/register", "/error", "/403", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/staff/timesheet/**").hasRole("STAFF")   // chấm công: chỉ nhân viên
                        .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(roleBasedSuccessHandler())
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling(ex -> ex.accessDeniedPage("/403"));
        return http.build();
    }

    /** Đăng nhập xong chuyển tới trang phù hợp với vai trò. */
    private AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            String target = "/";
            if (roles.contains("ROLE_ADMIN")) {
                target = "/admin";
            } else if (roles.contains("ROLE_STAFF")) {
                target = "/staff/orders";
            }
            response.sendRedirect(request.getContextPath() + target);
        };
    }
}
