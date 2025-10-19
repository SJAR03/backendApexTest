package com.sjar.orders.unit;

import com.sjar.orders.dto.CreateOrderRequest;
import com.sjar.orders.dto.OrderResponse;
import com.sjar.orders.kafka.OrderEventProducer;
import com.sjar.orders.mapper.OrderMapper;
import com.sjar.orders.model.Order;
import com.sjar.orders.model.OrderStatus;
import com.sjar.orders.repository.OrderRepository;
import com.sjar.orders.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        CreateOrderRequest request = new CreateOrderRequest("customer1",
                List.of(new CreateOrderRequest.ItemDto("Gatorade", 2, 38.0)));

        Order order = Order.builder()
                .id("1")
                .customerId("customer1")
                .status(OrderStatus.NEW)
                .build();

        OrderResponse expected = OrderResponse.builder()
                .id("1")
                .customerId("customer1")
                .status(OrderStatus.NEW.name())
                .build();

        when(orderMapper.toOrder(request)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        OrderResponse result = orderService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.NEW.name());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void shouldUpdateOrderStatusAndPublishEvent() {
        Order order = Order.builder()
                .id("1")
                .customerId("customer1")
                .status(OrderStatus.NEW)
                .build();

        when(orderRepository.findById("1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderMapper.toResponse(any())).thenReturn(OrderResponse.builder()
                .id("1")
                .customerId("customer1")
                .status(OrderStatus.IN_PROGRESS.name())
                .build());

        OrderResponse response = orderService.updateStatus("1", OrderStatus.IN_PROGRESS);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS.name());
        verify(orderEventProducer).sendStatusChangeEvent(eq("1"), eq(OrderStatus.NEW), eq(OrderStatus.IN_PROGRESS), any());
        verify(orderRepository).save(any(Order.class));
    }

}
