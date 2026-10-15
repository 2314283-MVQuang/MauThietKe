package com.trasua.service;

import com.trasua.entity.Feedback;
import com.trasua.entity.FeedbackStatus;
import com.trasua.entity.FeedbackTopic;
import com.trasua.entity.Role;
import com.trasua.entity.User;
import com.trasua.repository.FeedbackRepository;
import com.trasua.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Góp ý món của khách: khách gửi & theo dõi, quản lý trả lời & cập nhật tiến trình cải thiện. */
@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ProductRepository productRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, ProductRepository productRepository) {
        this.feedbackRepository = feedbackRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Feedback submit(User customer, Long productId, FeedbackTopic topic, String content) {
        if (customer.getRole() != Role.CUSTOMER) {
            throw new IllegalArgumentException("Chỉ khách hàng mới gửi góp ý");
        }
        String text = content == null ? "" : content.trim();
        if (text.length() < 10) {
            throw new IllegalArgumentException("Góp ý cần ít nhất 10 ký tự để quán hiểu rõ hơn");
        }
        if (text.length() > 1000) {
            throw new IllegalArgumentException("Góp ý tối đa 1000 ký tự");
        }
        Feedback f = new Feedback();
        f.setCustomer(customer);
        f.setProduct(productId == null ? null : productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy món #" + productId)));
        f.setTopic(topic == null ? FeedbackTopic.KHAC : topic);
        f.setContent(text);
        return feedbackRepository.save(f);
    }

    public List<Feedback> mine(User customer) {
        return feedbackRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    /** Các góp ý quán ĐÃ CẢI THIỆN - hiển thị công khai cho khách xem. */
    public List<Feedback> improvedHighlights() {
        return feedbackRepository.findTop6ByStatusOrderByRepliedAtDesc(FeedbackStatus.DA_CAI_THIEN);
    }

    /** Khách xoá góp ý của mình khi quán chưa xem. */
    @Transactional
    public void deleteOwn(Long id, User customer) {
        Feedback f = get(id);
        if (!f.getCustomer().getId().equals(customer.getId())) {
            throw new NotFoundException("Không tìm thấy góp ý #" + id);
        }
        if (f.getStatus() != FeedbackStatus.MOI) {
            throw new IllegalArgumentException("Quán đã xem góp ý này nên không xoá được");
        }
        feedbackRepository.delete(f);
    }

    // ===================== QUẢN LÝ =====================

    public List<Feedback> all(FeedbackStatus status, Long productId) {
        return feedbackRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(f -> status == null || f.getStatus() == status)
                .filter(f -> productId == null || (productId == 0 ? f.getProduct() == null
                        : f.getProduct() != null && f.getProduct().getId().equals(productId)))
                .toList();
    }

    public Map<FeedbackStatus, Long> countByStatus() {
        Map<FeedbackStatus, Long> map = new EnumMap<>(FeedbackStatus.class);
        for (FeedbackStatus s : FeedbackStatus.values()) {
            map.put(s, feedbackRepository.countByStatus(s));
        }
        return map;
    }

    /** Món bị góp ý nhiều nhất (ưu tiên cải thiện). */
    public Map<String, Long> topProducts(int limit) {
        Map<String, Long> counts = new LinkedHashMap<>();
        feedbackRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(f -> f.getStatus() != FeedbackStatus.DA_CAI_THIEN)
                .map(f -> f.getProduct() == null ? "Góp ý chung" : f.getProduct().getName())
                .forEach(name -> counts.merge(name, 1L, Long::sum));
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
    }

    @Transactional
    public Feedback reply(Long id, String reply, FeedbackStatus status, String adminUsername) {
        Feedback f = get(id);
        String text = reply == null ? null : reply.trim();
        if (text != null && text.length() > 1000) {
            throw new IllegalArgumentException("Phản hồi tối đa 1000 ký tự");
        }
        if (text != null && !text.isEmpty()) {
            f.setAdminReply(text);
        }
        FeedbackStatus next = status != null ? status
                : (f.getStatus() == FeedbackStatus.MOI ? FeedbackStatus.DA_XEM : f.getStatus());
        f.setStatus(next);
        f.setRepliedBy(adminUsername);
        f.setRepliedAt(LocalDateTime.now());
        return f;
    }

    public long countNew() {
        return feedbackRepository.countByStatus(FeedbackStatus.MOI);
    }

    private Feedback get(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy góp ý #" + id));
    }
}
