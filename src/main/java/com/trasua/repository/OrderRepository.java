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
}
