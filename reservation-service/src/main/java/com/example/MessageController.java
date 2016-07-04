package com.example;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
@EnableConfigurationProperties(SprioProperties.class)
public class MessageController {

	private final SprioProperties properties;

	@Autowired
	public MessageController(SprioProperties properties) {
		this.properties = properties;
	}

	@RequestMapping(method = GET)
	public String message() {
		return properties.message;
	}
}
