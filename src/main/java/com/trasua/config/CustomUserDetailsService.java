package com.trasua.config;

import com.trasua.entity.User;
import com.trasua.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Spring Security dùng lớp này để nạp tài khoản từ bảng users khi đăng nhập. */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())      // -> ROLE_ADMIN / ROLE_STAFF / ROLE_CUSTOMER
                .disabled(!user.isEnabled())       // tài khoản bị khoá không đăng nhập được
                .build();
    }
}
