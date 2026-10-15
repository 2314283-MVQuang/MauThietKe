package com.trasua.service;

import com.trasua.entity.Order;
import com.trasua.entity.OrderStatus;
import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.pattern.strategy.tier.CustomerStats;
import com.trasua.pattern.strategy.tier.CustomerTierClassifier;
import com.trasua.repository.OrderRepository;
import com.trasua.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** Thống kê & tra cứu khách hàng cho quản lý (role CUSTOMER). */
@Service
public class CustomerService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public CustomerService(UserRepository userRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    /** Tạo Context xếp hạng mới cho mỗi lần xem (mỗi request có thể chọn tiêu chí khác). */
    public CustomerTierClassifier classifier(String tierKey) {
        CustomerTierClassifier classifier = CustomerTierClassifier.withDefaults();
        classifier.use(tierKey);
        return classifier;
    }

    /**
     * Danh sách khách hàng kèm số đơn, tổng chi tiêu, hạng.
     *
     * @param keyword tìm theo tên / tên đăng nhập / SĐT / email (không phân biệt dấu)
     * @param sort    chi-tieu | so-don | moi-nhat | ten
     */
    public List<CustomerSummary> summaries(CustomerTierClassifier classifier, String keyword, String sort) {
        Map<Long, List<Order>> ordersByCustomer = orderRepository.findByCustomerIsNotNull().stream()
                .collect(Collectors.groupingBy(o -> o.getCustomer().getId()));
        String kw = normalize(keyword);
        Comparator<CustomerSummary> comparator = switch (sort == null ? "" : sort) {
            case "so-don" -> Comparator.comparingLong(CustomerSummary::getOrderCount).reversed();
            case "moi-nhat" -> Comparator.comparing((CustomerSummary c) -> c.getUser().getCreatedAt(),
                    Comparator.nullsLast(Comparator.reverseOrder()));
            case "ten" -> Comparator.comparing((CustomerSummary c) -> normalize(c.getUser().getFullName()));
            default -> Comparator.comparing(CustomerSummary::getTotalSpent).reversed();
        };
        return userRepository.findByRoleOrderByFullNameAsc(Role.CUSTOMER).stream()
                .filter(u -> kw.isEmpty() || matches(u, kw))
                .map(u -> summarize(u, ordersByCustomer.getOrDefault(u.getId(), List.of()), classifier))
                .sorted(comparator)
                .toList();
    }

    public CustomerSummary summary(User customer, CustomerTierClassifier classifier) {
        return summarize(customer, orderRepository.findByCustomerOrderByCreatedAtDesc(customer), classifier);
    }

    public User getCustomer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khách hàng #" + id));
        if (user.getRole() != Role.CUSTOMER) {
            throw new NotFoundException("Tài khoản #" + id + " không phải khách hàng");
        }
        return user;
    }

    public List<Order> ordersOf(User customer) {
        return orderRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    private CustomerSummary summarize(User user, List<Order> orders, CustomerTierClassifier classifier) {
        List<Order> notCancelled = orders.stream().filter(o -> o.getStatus() != OrderStatus.DA_HUY).toList();
        List<Order> completed = notCancelled.stream().filter(o -> o.getStatus() == OrderStatus.HOAN_THANH).toList();
        BigDecimal spent = completed.stream().map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        LocalDateTime last = orders.stream().map(Order::getCreatedAt).filter(java.util.Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
        // STRATEGY: hạng khách tính theo tiêu chí đang chọn
        var tier = classifier.classify(new CustomerStats(completed.size(), spent));
        return new CustomerSummary(user, notCancelled.size(), completed.size(), spent, last, tier);
    }

    private static boolean matches(User u, String kw) {
        return normalize(u.getFullName()).contains(kw) || normalize(u.getUsername()).contains(kw)
                || normalize(u.getPhone()).contains(kw) || normalize(u.getEmail()).contains(kw);
    }

    /** Bỏ dấu tiếng Việt + chữ thường để tìm kiếm. */
    static String normalize(String s) {
        if (s == null) {
            return "";
        }
        String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return noAccent.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT).trim();
    }
}
