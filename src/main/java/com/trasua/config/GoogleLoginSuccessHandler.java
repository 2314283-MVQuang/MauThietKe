package com.trasua.config;

import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Sau khi Google xác thực xong:
 *   1. Lấy email + tên từ Google.
 *   2. Tìm tài khoản có cùng email, chưa có thì tạo tài khoản KHÁCH HÀNG.
 *   3. Thay phiên đăng nhập Google bằng phiên đăng nhập của chính tài khoản đó
 *      -> mọi trang khác (giỏ hàng, đơn hàng, phân quyền) dùng y như đăng nhập thường.
 */
@Component
public class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public GoogleLoginSuccessHandler(UserService userService, CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User google = (OAuth2User) authentication.getPrincipal();
        String email = google.getAttribute("email");
        String name = google.getAttribute("name");
        Boolean verified = google.getAttribute("email_verified");

        if (email == null || Boolean.FALSE.equals(verified)) {
            logoutAndRedirect(request, response, "/login?error=google");
            return;
        }

        User user = userService.findOrCreateGoogleUser(email, name);
        if (!user.isEnabled()) {
            logoutAndRedirect(request, response, "/login?error=locked");
            return;
        }

        // Đăng nhập vào hệ thống bằng tài khoản nội bộ (tên đăng nhập + quyền lấy từ CSDL)
        UserDetails details = userDetailsService.loadUserByUsername(user.getUsername());
        Authentication local = UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(local);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);

        String target = user.getRole() == Role.ADMIN ? "/admin"
                : user.getRole() == Role.STAFF ? "/staff/orders" : "/";
        response.sendRedirect(request.getContextPath() + target);
    }

    private void logoutAndRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        response.sendRedirect(request.getContextPath() + url);
    }
}
