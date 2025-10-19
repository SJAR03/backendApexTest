package com.sjar.orders.integration;

import com.sjar.orders.OrdersApplication;
import org.springframework.boot.SpringApplication;

public class TestOrdersApplication {

	public static void main(String[] args) {
		SpringApplication.from(OrdersApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
