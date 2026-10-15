package com.trasua.repository;

import com.trasua.entity.Order;
import com.trasua.entity.OrderStatus;
import com.trasua.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerOrderByCreatedAtDesc(User customer);

    List<Order> findAllByOrderByCreatedAtDesc();

    /** Tất cả đơn có gắn tài khoản khách (dùng thống kê khách hàng). */
    List<Order> findByCustomerIsNotNull();

    /** Đơn tạo trong khoảng [from, to). */
    List<Order> findByCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);

    long countByStatusInAndCreatedAtLessThan(java.util.Collection<OrderStatus> statuses, LocalDateTime before);

    java.util.Optional<Order> findFirstByStatusInAndCreatedAtLessThanOrderByCreatedAtAsc(
            java.util.Collection<OrderStatus> statuses, LocalDateTime before);

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<Order> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime from, LocalDateTime to);

    List<Order> findTop8ByOrderByCreatedAtDesc();

    long countByStatus(OrderStatus status);

    /** Khách đã từng nhận một đơn HOÀN THÀNH có món này chưa? (điều kiện để được đánh giá) */
    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items i "
            + "WHERE o.customer.id = :userId AND o.status = :status AND i.productId = :productId")
    boolean hasPurchased(@Param("userId") Long userId, @Param("productId") Long productId,
                         @Param("status") OrderStatus status);

    /** Top đồ uống bán chạy: [tên món, tổng số ly, tổng doanh thu]. */
    @Query("SELECT i.productName, SUM(i.quantity), SUM(i.lineTotal) "
            + "FROM Order o JOIN o.items i WHERE o.status = :status "
            + "GROUP BY i.productName ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findBestSellers(@Param("status") OrderStatus status);

    /** Top đồ uống bán chạy trong khoảng thời gian [from, to). */
    @Query("SELECT i.productName, SUM(i.quantity), SUM(i.lineTotal) "
            + "FROM Order o JOIN o.items i WHERE o.status = :status AND o.createdAt >= :from AND o.createdAt < :to "
            + "GROUP BY i.productName ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findBestSellersBetween(@Param("status") OrderStatus status,
                                          @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
