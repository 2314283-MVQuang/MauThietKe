package com.trasua.service;

import com.trasua.entity.DailyClosing;
import com.trasua.entity.Order;
import com.trasua.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Tổng kết hoá đơn của 1 ngày (dùng cho trang Xử lý đơn và trang Tổng kết ngày của quản lý). */
public class DailyReport {

    /** Tiền đã thu theo một hình thức thanh toán. */
    public record PaymentRow(String label, long count, BigDecimal amount) {
    }

    /** Tổng hợp theo người thu tiền. */
    public static class CashierRow {
        private final String username;
        private final String fullName;
        private long orderCount;
        private BigDecimal cash = BigDecimal.ZERO;
        private BigDecimal transfer = BigDecimal.ZERO;
        private long cancelCount;
        private BigDecimal cancelledAmount = BigDecimal.ZERO;

        CashierRow(String username, String fullName) {
            this.username = username;
            this.fullName = fullName;
        }

        void addCash(BigDecimal v) { cash = cash.add(v); orderCount++; }
        void addTransfer(BigDecimal v) { transfer = transfer.add(v); orderCount++; }
        void addCancel(BigDecimal v) { cancelCount++; cancelledAmount = cancelledAmount.add(v); }

        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public long getOrderCount() { return orderCount; }
        public BigDecimal getCash() { return cash; }
        public BigDecimal getTransfer() { return transfer; }
        public BigDecimal getTotal() { return cash.add(transfer); }
        public long getCancelCount() { return cancelCount; }
        public BigDecimal getCancelledAmount() { return cancelledAmount; }
    }

    private final LocalDate date;
    private final List<Order> orders;
    private final Map<OrderStatus, Long> statusCounts;
    private final BigDecimal revenue;
    private final BigDecimal discountTotal;
    private final List<PaymentRow> payments;
    private final BigDecimal expectedCash;
    private final BigDecimal expectedTransfer;
    private final List<CashierRow> cashiers;
    private final List<Order> cancelled;
    private final List<Order> paidThenCancelled;
    private final List<Order> unpaid;
    private final List<Order> unfinished;
    private final DailyClosing closing;

    public DailyReport(LocalDate date, List<Order> orders, Map<OrderStatus, Long> statusCounts, BigDecimal revenue,
                       BigDecimal discountTotal, List<PaymentRow> payments, BigDecimal expectedCash,
                       BigDecimal expectedTransfer, List<CashierRow> cashiers, List<Order> cancelled,
                       List<Order> paidThenCancelled, List<Order> unpaid, List<Order> unfinished, DailyClosing closing) {
        this.date = date;
        this.orders = orders;
        this.statusCounts = statusCounts;
        this.revenue = revenue;
        this.discountTotal = discountTotal;
        this.payments = payments;
        this.expectedCash = expectedCash;
        this.expectedTransfer = expectedTransfer;
        this.cashiers = cashiers;
        this.cancelled = cancelled;
        this.paidThenCancelled = paidThenCancelled;
        this.unpaid = unpaid;
        this.unfinished = unfinished;
        this.closing = closing;
    }

    public LocalDate getDate() { return date; }
    public List<Order> getOrders() { return orders; }
    public int getOrderCount() { return orders.size(); }
    public Map<OrderStatus, Long> getStatusCounts() { return statusCounts; }
    public long getCompletedCount() { return statusCounts.getOrDefault(OrderStatus.HOAN_THANH, 0L); }
    public long getCancelledCount() { return statusCounts.getOrDefault(OrderStatus.DA_HUY, 0L); }
    /** Doanh thu = tổng tiền các đơn HOÀN THÀNH trong ngày. */
    public BigDecimal getRevenue() { return revenue; }
    public BigDecimal getDiscountTotal() { return discountTotal; }
    public List<PaymentRow> getPayments() { return payments; }
    /** Tiền mặt PHẢI CÓ trong két theo hoá đơn (đơn tiền mặt đã thu, không bị huỷ). */
    public BigDecimal getExpectedCash() { return expectedCash; }
    /** Chuyển khoản + MoMo đã xác nhận (đối chiếu sao kê ngân hàng / MoMo). */
    public BigDecimal getExpectedTransfer() { return expectedTransfer; }
    public BigDecimal getCollectedTotal() { return expectedCash.add(expectedTransfer); }
    public List<CashierRow> getCashiers() { return cashiers; }
    public List<Order> getCancelled() { return cancelled; }
    /** Đơn đã thu tiền rồi mới bị huỷ - cần kiểm tra kỹ (dấu hiệu quỵt bill). */
    public List<Order> getPaidThenCancelled() { return paidThenCancelled; }
    /** Đơn chưa thu tiền (không tính đơn huỷ). */
    public List<Order> getUnpaid() { return unpaid; }
    /** Đơn chưa kết thúc (chưa hoàn thành / chưa huỷ). */
    public List<Order> getUnfinished() { return unfinished; }
    public DailyClosing getClosing() { return closing; }
    public boolean isClosed() { return closing != null; }

    /** Hoá đơn bị sửa sau khi chốt két -> tiền mặt theo hoá đơn hiện tại khác lúc chốt. */
    public boolean isChangedAfterClosing() {
        return closing != null && closing.getExpectedCash().compareTo(expectedCash) != 0;
    }

    /** Số cảnh báo cần quản lý xem. */
    public int getWarningCount() {
        return paidThenCancelled.size() + unfinished.size() + unpaid.size();
    }
}
