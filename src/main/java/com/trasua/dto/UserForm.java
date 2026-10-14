package com.trasua.dto;

import com.trasua.entity.Role;
import com.trasua.entity.User;

/** Form thêm / sửa tài khoản dành cho quản lý. Mật khẩu để trống khi sửa = giữ nguyên. */
public class UserForm {

    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private Role role = Role.STAFF;
    private boolean enabled = true;

    public static UserForm from(User user) {
        UserForm form = new UserForm();
        form.id = user.getId();
        form.username = user.getUsername();
        form.fullName = user.getFullName();
        form.phone = user.getPhone();
        form.email = user.getEmail();
        form.address = user.getAddress();
        form.role = user.getRole();
        form.enabled = user.isEnabled();
        return form;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
