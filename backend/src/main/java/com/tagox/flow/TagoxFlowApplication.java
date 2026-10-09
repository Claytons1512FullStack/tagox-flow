package com.tagox.flow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
		excludeName = "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration"
)
public class TagoxFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(TagoxFlowApplication.class, args);
	}

}