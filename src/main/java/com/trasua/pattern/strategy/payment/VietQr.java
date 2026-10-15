package com.trasua.pattern.strategy.payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tạo nội dung mã VietQR (chuẩn EMVCo / NAPAS 247) - mã mà app ngân hàng và MoMo đều quét được.
 *
 * Mã QR thực chất là một chuỗi gồm nhiều trường "ID - độ dài - giá trị" (TLV), ví dụ:
 *   00 02 01            phiên bản
 *   01 02 12            12 = QR động (có số tiền), 11 = QR tĩnh
 *   38 ..               thông tin người nhận: A000000727 (NAPAS) + BIN ngân hàng + số tài khoản
 *   53 03 704           đơn vị tiền VND
 *   54 ..  64800        SỐ TIỀN cần thanh toán
 *   58 02 VN
 *   62 .. 08 .. TS123   nội dung chuyển khoản (mã đơn)
 *   63 04 ABCD          mã kiểm tra CRC16 của toàn bộ chuỗi phía trước
 */
public final class VietQr {

    /** Mã định danh NAPAS trong trường 38. */
    public static final String NAPAS_GUID = "A000000727";

    private VietQr() {
    }

    /** QR chuyển khoản tới tài khoản ngân hàng (BIN: Vietcombank = 970436). */
    public static String forBankAccount(String bin, String accountNumber, BigDecimal amount, String purpose) {
        String beneficiary = tlv("00", bin) + tlv("01", accountNumber);
        String merchantInfo = tlv("00", NAPAS_GUID) + tlv("01", beneficiary) + tlv("02", "QRIBFTTA");
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("00", "01");
        fields.put("01", "11");
        fields.put("38", merchantInfo);
        fields.put("53", "704");
        fields.put("58", "VN");
        return build(withAmount(fields, amount, purpose));
    }

    /**
     * Lấy mã QR TĨNH có sẵn (VD QR MoMo của quán) rồi gắn thêm số tiền + nội dung -> QR ĐỘNG.
     *
     * @param basePayload chuỗi đọc được từ ảnh QR tĩnh (bắt đầu bằng "000201")
     */
    public static String withAmount(String basePayload, BigDecimal amount, String purpose) {
        return build(withAmount(parse(basePayload), amount, purpose));
    }

    private static Map<String, String> withAmount(Map<String, String> fields, BigDecimal amount, String purpose) {
        Map<String, String> result = new LinkedHashMap<>(fields);
        result.remove("63");                                  // CRC sẽ tính lại
        if (amount != null && amount.signum() > 0) {
            result.put("01", "12");                           // QR động
            result.put("54", amount.setScale(0, RoundingMode.HALF_UP).toPlainString());
        }
        String note = clean(purpose);
        if (!note.isEmpty()) {
            Map<String, String> additional = parse(result.getOrDefault("62", ""));
            additional.put("08", note);                       // 08 = mục đích giao dịch (nội dung CK)
            result.put("62", join(additional));
        }
        return result;
    }

    /** Ghép các trường theo đúng thứ tự ID tăng dần rồi thêm CRC. */
    static String build(Map<String, String> fields) {
        Map<String, String> sorted = new java.util.TreeMap<>(fields);
        String body = join(sorted) + "6304";
        return body + crc16(body);
    }

    /** Tách chuỗi TLV thành bảng ID -> giá trị. */
    public static Map<String, String> parse(String payload) {
        Map<String, String> map = new LinkedHashMap<>();
        int i = 0;
        while (payload != null && i + 4 <= payload.length()) {
            String id = payload.substring(i, i + 2);
            int len = Integer.parseInt(payload.substring(i + 2, i + 4));
            if (i + 4 + len > payload.length()) {
                throw new IllegalArgumentException("Chuỗi QR không hợp lệ ở trường " + id);
            }
            map.put(id, payload.substring(i + 4, i + 4 + len));
            i += 4 + len;
        }
        return map;
    }

    /** Kiểm tra mã CRC ở cuối chuỗi có đúng không. */
    public static boolean isValid(String payload) {
        if (payload == null || payload.length() < 8 || !payload.substring(payload.length() - 8, payload.length() - 4).equals("6304")) {
            return false;
        }
        String body = payload.substring(0, payload.length() - 4);
        return crc16(body).equalsIgnoreCase(payload.substring(payload.length() - 4));
    }

    /** CRC-16/CCITT-FALSE (đa thức 0x1021, khởi tạo 0xFFFF) - theo chuẩn EMVCo. */
    public static String crc16(String data) {
        int crc = 0xFFFF;
        for (byte b : data.getBytes(StandardCharsets.UTF_8)) {
            crc ^= (b & 0xFF) << 8;
            for (int k = 0; k < 8; k++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }

    private static String join(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        fields.forEach((id, value) -> sb.append(tlv(id, value)));
        return sb.toString();
    }

    private static String tlv(String id, String value) {
        if (value.length() > 99) {
            throw new IllegalArgumentException("Trường " + id + " quá dài");
        }
        return id + String.format("%02d", value.length()) + value;
    }

    /** Nội dung CK: bỏ dấu, chỉ giữ chữ/số/khoảng trắng, tối đa 25 ký tự. */
    static String clean(String s) {
        if (s == null) {
            return "";
        }
        String noAccent = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D');
        String ascii = noAccent.replaceAll("[^A-Za-z0-9 ]", "").trim();
        return ascii.length() > 25 ? ascii.substring(0, 25) : ascii;
    }
}
