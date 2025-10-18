package com.sjar.orders.service;

import com.sjar.orders.dto.CreateOrderRequest;
import com.sjar.orders.dto.OrderResponse;
import com.sjar.orders.exception.NotFoundException;
import com.sjar.orders.kafka.OrderEventProducer;
import com.sjar.orders.mapper.OrderMapper;
import com.sjar.orders.model.Order;
import com.sjar.orders.model.OrderStatus;
import com.sjar.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventProducer producer;

    public OrderResponse create(CreateOrderRequest request) {
        Order order = orderMapper.toOrder(request);

        //order.setId(UUID.randomUUID().toString());
        //order.setCreatedAt(Instant.now());
        //order.setUpdatedAt(Instant.now());
        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    @Cacheable(value = "ordersCache", key = "#id", unless = "#result == null")
    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        return orderMapper.toResponse(order);
    }

    public List<OrderResponse> getAllOrders(OrderStatus status, String customerId) {
        List<Order> orderList;
        if (status != null && customerId != null) {
            orderList = orderRepository.findByStatusAndCustomerId(status, customerId);
        } else if (status != null) {
            orderList = orderRepository.findByStatus(status);
        } else if (customerId != null) {
            orderList = orderRepository.findByCustomerId(customerId);
        } else {
            orderList = orderRepository.findAll();
        }
        return orderList.stream().map(orderMapper::toResponse).collect(Collectors.toList());
    }

    @CacheEvict(value = "ordersCache", key = "#id")
    public OrderResponse updateStatus(String id, OrderStatus newStatus) {
        Order o = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        OrderStatus old = o.getStatus();
        o.setStatus(newStatus);
        o.setUpdatedAt(Instant.now());
        orderRepository.save(o);

        // publish event
        producer.sendStatusChangeEvent(id, old, newStatus, Instant.now());

        return orderMapper.toResponse(o);
    }
}
