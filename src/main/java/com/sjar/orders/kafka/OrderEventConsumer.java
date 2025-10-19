package com.sjar.orders.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjar.orders.model.OrderAudit;
import com.sjar.orders.repository.OrderAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderAuditRepository auditRepository;

    @KafkaListener(topics = "${spring.kafka.topic:orders.events}", groupId = "orders-service-group")
    public void consume(String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            log.info("Received Kafka event: {}", payload);

            String orderId = (String) payload.get("orderId");
            String oldStatus = (String) payload.get("oldStatus");
            String newStatus = (String) payload.get("newStatus");
            String timestamp = (String) payload.get("timestamp");

            OrderAudit audit = OrderAudit.builder()
                    .orderId(orderId)
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .timestamp(Instant.parse(timestamp))
                    .build();

            auditRepository.save(audit);
            log.info("Saved audit record for order {}", orderId);

        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", message, e);
        }
    }
}