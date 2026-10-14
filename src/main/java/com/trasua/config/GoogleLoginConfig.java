package com.trasua.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * Bật "Đăng nhập bằng Google" khi đã điền app.google.client-id trong application.properties.
 * Chưa điền thì lớp này không tạo bean nào -> web vẫn chạy, chỉ là nút Google chưa dùng được.
 */
@Configuration
@ConditionalOnExpression("!'${app.google.client-id:}'.isBlank()")
public class GoogleLoginConfig {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${app.google.client-id}") String clientId,
            @Value("${app.google.client-secret}") String clientSecret) {
        // CommonOAuth2Provider.GOOGLE đã có sẵn địa chỉ đăng nhập, token, scope "openid profile email"
        return new InMemoryClientRegistrationRepository(
                CommonOAuth2Provider.GOOGLE.getBuilder("google")
                        .clientId(clientId.trim())
                        .clientSecret(clientSecret.trim())
                        .build());
    }
}
