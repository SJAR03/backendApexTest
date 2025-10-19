package com.sjar.orders.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjar.orders.controller.OrderController;
import com.sjar.orders.dto.CreateOrderRequest;
import com.sjar.orders.dto.OrderResponse;
import com.sjar.orders.dto.UpdateStatusRequest;
import com.sjar.orders.model.OrderStatus;
import com.sjar.orders.repository.OrderAuditRepository;
import com.sjar.orders.service.OrderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class)
@ActiveProfiles("test")
@Import(OrderControllerTest.TestConfig.class)
class OrderControllerTest {

    @SpringBootApplication(scanBasePackages = "com.sjar.orders.controller")
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderAuditRepository orderAuditRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateOrder() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest(
                "customer1",
                List.of(new CreateOrderRequest.ItemDto("Pepsi", 1, 25.0))
        );

        OrderResponse resp = OrderResponse.builder()
                .id("1")
                .customerId("customer1")
                .status(OrderStatus.NEW.name())
                .createdAt(Instant.now())
                .build();

        when(orderService.create(Mockito.any())).thenReturn(resp);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void shouldUpdateOrderStatus() throws Exception {
        UpdateStatusRequest req = new UpdateStatusRequest(OrderStatus.IN_PROGRESS.name());
        OrderResponse resp = OrderResponse.builder()
                .id("1")
                .customerId("customer1")
                .status(OrderStatus.IN_PROGRESS.name())
                .build();

        when(orderService.updateStatus("1", OrderStatus.IN_PROGRESS)).thenReturn(resp);

        mockMvc.perform(patch("/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}