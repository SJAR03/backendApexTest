package com.sjar.orders.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collation = "orders")
public class Order {
    @Id
    private String id; // uuid
    private String customerId;
    private OrderStatus status;
    private List<Item> items;
    @CreatedDate
    private Instant CreatedAt;
    @LastModifiedDate
    private Instant UpdatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private String sku;
        private Integer quantity;
        private Double price;
    }
}
