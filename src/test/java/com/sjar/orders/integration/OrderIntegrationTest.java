package com.sjar.orders.integration;

import com.sjar.orders.dto.CreateOrderRequest;
import com.sjar.orders.dto.UpdateStatusRequest;
import com.sjar.orders.model.OrderStatus;
import com.sjar.orders.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class OrderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldCreateOrder_andUpdateStatusSuccessfully() {
        // GIVEN: valid CreateOrderRequest
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId("234567")
                .items(List.of(
                        new CreateOrderRequest.ItemDto("Pepsi", 2, 25.0),
                        new CreateOrderRequest.ItemDto("Gatorade", 3, 38.0)
                ))
                .build();

        // WHEN: execute POST /orders
        ResponseEntity<Map> createResponse = restTemplate.postForEntity("/orders", request, Map.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        String orderId = (String) createResponse.getBody().get("id");
        assertThat(orderId).isNotEmpty();

        // THEN: the order its save in mongo
        assertThat(orderRepository.findById(orderId)).isPresent();

        // WHEN: update the order status
        UpdateStatusRequest statusReq = new UpdateStatusRequest(OrderStatus.IN_PROGRESS.name());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateStatusRequest> httpEntity = new HttpEntity<>(statusReq, headers);

        ResponseEntity<Map> patchResponse = restTemplate.exchange(
                "/orders/" + orderId + "/status", HttpMethod.PATCH, httpEntity, Map.class
        );

        // THEN: the status changed
        assertThat(patchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(patchResponse.getBody().get("status")).isEqualTo(OrderStatus.IN_PROGRESS.name());
    }
}
