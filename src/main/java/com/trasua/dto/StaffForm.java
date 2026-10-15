package com.trasua.dto;

import com.trasua.entity.EmploymentType;
import com.trasua.entity.User;

import java.math.BigDecimal;

/** Form thêm / sửa NHÂN VIÊN (kèm thông tin lương). Mật khẩu để trống khi sửa = giữ nguyên. */
public class StaffForm {

    private Long id;
    private String username;
    private String password;
    private String fullName;
    private String phone;
    private String email;
    private EmploymentType employmentType = EmploymentType.PART_TIME;
    private BigDecimal hourlyRate = BigDecimal.valueOf(25_000);
    private BigDecimal baseSalary;
    private boolean enabled = true;

    public static StaffForm from(User user) {
        StaffForm form = new StaffForm();
        form.id = user.getId();
        form.username = user.getUsername();
        form.fullName = user.getFullName();
        form.phone = user.getPhone();
        form.email = user.getEmail();
        form.employmentType = user.getEmploymentType();
        form.hourlyRate = user.getHourlyRate().setScale(0, java.math.RoundingMode.HALF_UP);
        form.baseSalary = user.getBaseSalary().signum() == 0 ? null
                : user.getBaseSalary().setScale(0, java.math.RoundingMode.HALF_UP);
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
    public EmploymentType getEmploymentType() { return employmentType; }
    public void setEmploymentType(EmploymentType employmentType) { this.employmentType = employmentType; }
    public BigDecimal getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(BigDecimal hourlyRate) { this.hourlyRate = hourlyRate; }
    public BigDecimal getBaseSalary() { return baseSalary; }
    public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
