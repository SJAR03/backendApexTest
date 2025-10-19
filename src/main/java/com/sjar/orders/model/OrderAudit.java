package com.sjar.orders.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "order_audit")
public class OrderAudit {

    @Id
    private String id;
    private String orderId;
    private String oldStatus;
    private String newStatus;
    private Instant timestamp;
}
