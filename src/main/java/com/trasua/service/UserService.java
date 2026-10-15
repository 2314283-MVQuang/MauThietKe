package com.trasua.service;

import com.trasua.dto.RegisterForm;
import com.trasua.dto.StaffForm;
import com.trasua.dto.UserForm;
import com.trasua.entity.EmploymentType;
import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản " + username));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản #" + id));
    }

    public List<User> findAll() {
        return userRepository.findAllByOrderByRoleAscUsernameAsc();
    }

    /** Khách tự đăng ký -> luôn là CUSTOMER. */
    @Transactional
    public User register(RegisterForm form) {
        String username = trim(form.getUsername());
        if (username == null || !username.matches("[a-zA-Z0-9_.]{4,30}")) {
            throw new IllegalArgumentException("Tên đăng nhập 4-30 ký tự, chỉ gồm chữ không dấu, số, dấu _ hoặc .");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        validatePassword(form.getPassword());
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu nhập lại không khớp");
        }
        if (trim(form.getFullName()) == null) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên");
        }
        validatePhone(form.getPhone());

        User user = new User(username, passwordEncoder.encode(form.getPassword()), trim(form.getFullName()), Role.CUSTOMER);
        user.setPhone(trim(form.getPhone()));
        user.setEmail(trim(form.getEmail()));
        user.setAddress(trim(form.getAddress()));
        return userRepository.save(user);
    }

    /** Quản lý thêm / sửa tài khoản. */
    @Transactional
    public User saveByAdmin(UserForm form, String currentAdminUsername) {
        User user;
        if (form.getId() == null) {
            String username = trim(form.getUsername());
            if (username == null || !username.matches("[a-zA-Z0-9_.]{4,30}")) {
                throw new IllegalArgumentException("Tên đăng nhập 4-30 ký tự, chỉ gồm chữ không dấu, số, dấu _ hoặc .");
            }
            if (userRepository.existsByUsername(username)) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
            }
            validatePassword(form.getPassword());
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        } else {
            user = getById(form.getId());
            if (form.getPassword() != null && !form.getPassword().isBlank()) {
                validatePassword(form.getPassword());
                user.setPassword(passwordEncoder.encode(form.getPassword()));
            }
            // Không cho admin tự hạ quyền / tự khoá chính mình
            if (user.getUsername().equals(currentAdminUsername)
                    && (form.getRole() != Role.ADMIN || !form.isEnabled())) {
                throw new IllegalArgumentException("Bạn không thể tự hạ quyền hoặc khoá tài khoản của chính mình");
            }
        }
        if (trim(form.getFullName()) == null) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên");
        }
        validatePhone(form.getPhone());
        user.setFullName(trim(form.getFullName()));
        user.setPhone(trim(form.getPhone()));
        user.setEmail(trim(form.getEmail()));
        user.setAddress(trim(form.getAddress()));
        user.setRole(form.getRole() == null ? Role.CUSTOMER : form.getRole());
        user.setEnabled(form.isEnabled());
        return userRepository.save(user);
    }

    /** Quản lý thêm / sửa NHÂN VIÊN kèm thông tin lương (vai trò luôn là STAFF). */
    @Transactional
    public User saveStaff(StaffForm form) {
        User user;
        if (form.getId() == null) {
            String username = trim(form.getUsername());
            if (username == null || !username.matches("[a-zA-Z0-9_.]{4,30}")) {
                throw new IllegalArgumentException("Tên đăng nhập 4-30 ký tự, chỉ gồm chữ không dấu, số, dấu _ hoặc .");
            }
            if (userRepository.existsByUsername(username)) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
            }
            validatePassword(form.getPassword());
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(form.getPassword()));
            user.setRole(Role.STAFF);
        } else {
            user = getById(form.getId());
            if (user.getRole() != Role.STAFF) {
                throw new IllegalArgumentException("Tài khoản này không phải nhân viên");
            }
            if (form.getPassword() != null && !form.getPassword().isBlank()) {
                validatePassword(form.getPassword());
                user.setPassword(passwordEncoder.encode(form.getPassword()));
            }
        }
        if (trim(form.getFullName()) == null) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên");
        }
        validatePhone(form.getPhone());
        EmploymentType type = form.getEmploymentType() == null ? EmploymentType.PART_TIME : form.getEmploymentType();
        BigDecimal rate = form.getHourlyRate();
        if (rate == null || rate.signum() <= 0) {
            throw new IllegalArgumentException("Lương theo giờ phải lớn hơn 0");
        }
        BigDecimal base = form.getBaseSalary();
        if (type == EmploymentType.FULL_TIME && (base == null || base.signum() <= 0)) {
            throw new IllegalArgumentException("Nhân viên toàn thời gian cần có lương cứng/tháng");
        }
        if (base != null && base.signum() < 0) {
            throw new IllegalArgumentException("Lương cứng không được âm");
        }
        user.setFullName(trim(form.getFullName()));
        user.setPhone(trim(form.getPhone()));
        user.setEmail(trim(form.getEmail()));
        user.setEmploymentType(type);
        user.setHourlyRate(rate);
        user.setBaseSalary(type == EmploymentType.FULL_TIME ? base : null);
        user.setEnabled(form.isEnabled());
        return userRepository.save(user);
    }

    public List<User> findByRole(Role role) {
        return userRepository.findByRoleOrderByFullNameAsc(role);
    }

    @Transactional
    public void toggleEnabled(Long id, String currentAdminUsername) {
        User user = getById(id);
        if (user.getUsername().equals(currentAdminUsername)) {
            throw new IllegalArgumentException("Bạn không thể khoá tài khoản của chính mình");
        }
        user.setEnabled(!user.isEnabled());
    }

    @Transactional
    public void updateProfile(String username, String fullName, String phone, String email, String address) {
        User user = getByUsername(username);
        if (trim(fullName) == null) {
            throw new IllegalArgumentException("Vui lòng nhập họ tên");
        }
        validatePhone(phone);
        user.setFullName(trim(fullName));
        user.setPhone(trim(phone));
        user.setEmail(trim(email));
        user.setAddress(trim(address));
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword, String confirm) {
        User user = getByUsername(username);
        if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        validatePassword(newPassword);
        if (!newPassword.equals(confirm)) {
            throw new IllegalArgumentException("Mật khẩu nhập lại không khớp");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    /**
     * Đăng nhập bằng Google: tìm tài khoản theo email; chưa có thì tạo tài khoản KHÁCH HÀNG mới.
     * Mật khẩu là chuỗi ngẫu nhiên (người dùng luôn đăng nhập qua Google).
     */
    @Transactional
    public User findOrCreateGoogleUser(String email, String fullName) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Tài khoản Google không cung cấp email");
        }
        String mail = email.trim().toLowerCase();
        return userRepository.findFirstByEmailIgnoreCase(mail).orElseGet(() -> {
            User user = new User(uniqueUsernameFrom(mail),
                    passwordEncoder.encode(java.util.UUID.randomUUID().toString()),
                    (fullName == null || fullName.isBlank()) ? mail : fullName.trim(),
                    Role.CUSTOMER);
            user.setEmail(mail);
            return userRepository.save(user);
        });
    }

    /** "Nguyen.Van-A@gmail.com" -> "nguyen.vana", thêm số nếu đã có người dùng. */
    private String uniqueUsernameFrom(String email) {
        String base = email.substring(0, email.indexOf('@') > 0 ? email.indexOf('@') : email.length())
                .toLowerCase().replaceAll("[^a-z0-9_.]", "");
        if (base.length() < 4) {
            base = (base + "user").substring(0, 4);
        }
        if (base.length() > 26) {
            base = base.substring(0, 26);
        }
        String candidate = base;
        int i = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + i++;
        }
        return candidate;
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 5) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 5 ký tự");
        }
    }

    private static void validatePhone(String phone) {
        String p = trim(phone);
        if (p != null && !p.matches("0\\d{9,10}")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (10-11 số, bắt đầu bằng 0)");
        }
    }

    private static String trim(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
