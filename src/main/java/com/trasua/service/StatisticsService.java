package com.trasua.service;

import com.trasua.entity.Order;
import com.trasua.entity.OrderStatus;
import com.trasua.entity.Role;
import com.trasua.repository.OrderRepository;
import com.trasua.repository.ProductRepository;
import com.trasua.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Số liệu cho trang thống kê của quản lý. Doanh thu chỉ tính các đơn HOÀN THÀNH. */
@Service
public class StatisticsService {

    public record DayRevenue(String label, BigDecimal amount, int percent) {
    }

    public record BestSeller(String productName, long quantity, BigDecimal revenue) {
    }

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public StatisticsService(OrderRepository orderRepository, ProductRepository productRepository,
                             UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public BigDecimal revenueBetween(LocalDateTime from, LocalDateTime to) {
        return orderRepository.findByStatusAndCreatedAtBetween(OrderStatus.HOAN_THANH, from, to).stream()
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal revenueToday() {
        LocalDate today = LocalDate.now();
        return revenueBetween(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    public BigDecimal revenueThisMonth() {
        LocalDate first = LocalDate.now().withDayOfMonth(1);
        return revenueBetween(first.atStartOfDay(), first.plusMonths(1).atStartOfDay());
    }

    /** Doanh thu 7 ngày gần nhất (kèm % để vẽ thanh biểu đồ). */
    public List<DayRevenue> last7Days() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        Map<String, BigDecimal> data = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            data.put(day.format(fmt), revenueBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay()));
        }
        BigDecimal max = data.values().stream().reduce(BigDecimal.ZERO, BigDecimal::max);
        List<DayRevenue> result = new ArrayList<>();
        data.forEach((label, amount) -> {
            int percent = max.signum() == 0 ? 0
                    : amount.multiply(BigDecimal.valueOf(100)).divide(max, 0, java.math.RoundingMode.HALF_UP).intValue();
            result.add(new DayRevenue(label, amount, percent));
        });
        return result;
    }

    public List<BestSeller> bestSellers(int limit) {
        return orderRepository.findBestSellers(OrderStatus.HOAN_THANH).stream()
                .limit(limit)
                .map(row -> new BestSeller((String) row[0],
                        ((Number) row[1]).longValue(),
                        row[2] == null ? BigDecimal.ZERO : new BigDecimal(row[2].toString())))
                .toList();
    }

    public Map<OrderStatus, Long> countByStatus() {
        Map<OrderStatus, Long> map = new LinkedHashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            map.put(status, orderRepository.countByStatus(status));
        }
        return map;
    }

    public List<Order> recentOrders() {
        return orderRepository.findTop8ByOrderByCreatedAtDesc();
    }

    public long productCount() {
        return productRepository.count();
    }

    public long customerCount() {
        return userRepository.countByRole(Role.CUSTOMER);
    }

    public long staffCount() {
        return userRepository.countByRole(Role.STAFF);
    }
}
