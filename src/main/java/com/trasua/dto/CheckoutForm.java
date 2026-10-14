package com.trasua.dto;

import com.trasua.entity.OrderType;
import com.trasua.entity.PaymentMethod;

import java.math.BigDecimal;

/** Dữ liệu form thanh toán giỏ hàng (dùng chung cho khách online và nhân viên tại quầy). */
public class CheckoutForm {

    private String customerName;
    private String phone;
    private OrderType orderType = OrderType.MANG_DI;
    private String deliveryAddress;
    private String note;
    private PaymentMethod paymentMethod = PaymentMethod.TIEN_MAT;
    /** Tiền khách đưa - nhân viên nhập khi thu tiền mặt tại quầy. */
    private BigDecimal cashReceived;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getCashReceived() { return cashReceived; }
    public void setCashReceived(BigDecimal cashReceived) { this.cashReceived = cashReceived; }
}
