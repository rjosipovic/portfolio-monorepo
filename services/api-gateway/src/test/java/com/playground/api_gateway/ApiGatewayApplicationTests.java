package com.playground.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.cloud.consul.config.enabled=false",
        "spring.cloud.consul.discovery.enabled=false"
})
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}
}
