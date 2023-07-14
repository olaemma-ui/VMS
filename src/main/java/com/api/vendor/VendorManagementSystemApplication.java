package com.api.vendor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableAutoConfiguration
@EnableConfigurationProperties
public class VendorManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(VendorManagementSystemApplication.class, args);
	}

}
