package com.trasua.pattern;

import com.trasua.pattern.strategy.payment.BankTransferPaymentStrategy;
import com.trasua.pattern.strategy.payment.CashPaymentStrategy;
import com.trasua.pattern.strategy.payment.MomoPaymentStrategy;
import com.trasua.pattern.strategy.payment.PaymentStrategy;
import com.trasua.pattern.strategy.payment.VietQr;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Kiểm thử mã QR thanh toán kèm số tiền (VietQR) - mỗi PaymentStrategy tự tạo QR của mình. */
class VietQrTest {

    /** Nội dung đọc từ ảnh QR Vietcombank thật của quán (QR tĩnh, chưa có số tiền). */
    private static final String VCB_STATIC =
            "00020101021138540010A00000072701240006970436011010354074250208QRIBFTTA53037045802VN63045037";
    private static final String MOMO_STATIC =
            "00020101021138630010A000000727013300069710250119PSP26287122000000850208QRIBFTTA53037045802VN"
                    + "62450515MOMOW2W860065990622knESoD5DJ7HBJYMXfoAGPg63042416";

    @Test
    void crcKhopVoiQrThatCuaNganHang() {
        assertTrue(VietQr.isValid(VCB_STATIC));
        assertTrue(VietQr.isValid(MOMO_STATIC));
        assertFalse(VietQr.isValid(VCB_STATIC.replace("1035407425", "1035407426")));
    }

    @Test
    void taoLaiDungQrTinhVietcombank() {
        assertEquals(VCB_STATIC, VietQr.forBankAccount("970436", "1035407425", null, null));
    }

    @Test
    void qrDongCoSoTienVaNoiDung() {
        String qr = VietQr.forBankAccount("970436", "1035407425", new BigDecimal("64800.00"), "TS261014122450685");
        Map<String, String> f = VietQr.parse(qr);
        assertEquals("12", f.get("01"));                 // QR động
        assertEquals("64800", f.get("54"));              // số tiền
        assertEquals("TS261014122450685", VietQr.parse(f.get("62")).get("08"));
        assertTrue(VietQr.isValid(qr));
    }

    @Test
    void momoGiuNguyenTaiKhoanVaThemSoTien() {
        String qr = VietQr.withAmount(MOMO_STATIC, BigDecimal.valueOf(38_000), "TS261014122750587");
        Map<String, String> f = VietQr.parse(qr);
        assertEquals(VietQr.parse(MOMO_STATIC).get("38"), f.get("38"));     // vẫn là ví MoMo của quán
        assertEquals("38000", f.get("54"));
        Map<String, String> extra = VietQr.parse(f.get("62"));
        assertEquals("MOMOW2W86006599", extra.get("05"));
        assertEquals("TS261014122750587", extra.get("08"));
        assertTrue(VietQr.isValid(qr));
    }

    @Test
    void moiChienLuocTuTaoQrCuaMinh() {
        PaymentStrategy bank = new BankTransferPaymentStrategy("Vietcombank", "1035407425", "MAI VAN QUANG", null, "970436");
        PaymentStrategy momo = new MomoPaymentStrategy("MAI VAN QUANG", null, MOMO_STATIC);
        PaymentStrategy cash = new CashPaymentStrategy();
        assertNotNull(bank.buildQrPayload(BigDecimal.TEN, "TS1"));
        assertNotNull(momo.buildQrPayload(BigDecimal.TEN, "TS1"));
        assertNull(cash.buildQrPayload(BigDecimal.TEN, "TS1"));              // tiền mặt: không có QR
        assertTrue(bank.getQrReceiver().contains("1035407425"));
    }
}
