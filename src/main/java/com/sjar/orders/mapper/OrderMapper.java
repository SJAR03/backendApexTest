package com.sjar.orders.mapper;

import com.sjar.orders.dto.CreateOrderRequest;
import com.sjar.orders.dto.OrderResponse;
import com.sjar.orders.model.Order;
import com.sjar.orders.model.OrderStatus;
import org.mapstruct.Mapper;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    default Order toOrder(CreateOrderRequest request) {
        if (request == null) return null;
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(OrderStatus.NEW);
        order.setItems(request.getItems().stream()
                .map(i -> Order.Item.builder().sku(i.getSku()).quantity(i.getQuantity()).price(i.getPrice()).build())
                .collect(Collectors.toList())
        );
        return order;
    }

    default OrderResponse toResponse(Order order) {
        if (order == null) return null;
        OrderResponse resp = new OrderResponse();
        resp.setId(order.getId());
        resp.setCustomerId(order.getCustomerId());
        resp.setStatus(order.getStatus().name());
        resp.setCreatedAt(order.getCreatedAt());
        resp.setUpdatedAt(order.getUpdatedAt());
        resp.setItems(order.getItems().stream().map(i -> new OrderResponse.ItemDto(i.getSku(), i.getQuantity(), i.getPrice())).collect(Collectors.toList()));
        return resp;
    }
}
