package com.sjar.orders.kafka;

import com.sjar.orders.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topic:orders.events}")
    private String topic;

    public void sendStatusChangeEvent(String orderId, OrderStatus oldStatus, OrderStatus newStatus, Instant timestamp) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderId", orderId);
        payload.put("oldStatus", oldStatus == null ? null : oldStatus.name());
        payload.put("newStatus", newStatus.name());
        payload.put("timestamp", timestamp.toString());
        kafkaTemplate.send(topic, orderId, payload);
    }
}
