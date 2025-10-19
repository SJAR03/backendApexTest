package com.sjar.orders.controller;

import com.sjar.orders.dto.CreateOrderRequest;
import com.sjar.orders.dto.OrderResponse;
import com.sjar.orders.dto.UpdateStatusRequest;
import com.sjar.orders.model.OrderAudit;
import com.sjar.orders.model.OrderStatus;
import com.sjar.orders.repository.OrderAuditRepository;
import com.sjar.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    // I know this is not the best, but just for fast check on saved audits
    private final OrderAuditRepository orderAuditRepository;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(orderService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll(@RequestParam(required = false) String status, @RequestParam(required = false) String customerId) {
        OrderStatus st = status == null ? null : OrderStatus.valueOf(status);
        return ResponseEntity.ok(orderService.getAllOrders(st, customerId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable String id, @RequestBody UpdateStatusRequest request) {
        OrderStatus newStatus = OrderStatus.valueOf(request.getStatus());
        return ResponseEntity.ok(orderService.updateStatus(id, newStatus));
    }

    @GetMapping("/audit")
    public ResponseEntity<List<OrderAudit>> getAudits() {
        return ResponseEntity.ok(orderAuditRepository.findAll());
    }
}
