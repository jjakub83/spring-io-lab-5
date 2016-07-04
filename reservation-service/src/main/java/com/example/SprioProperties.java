package com.example;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "sprio")
@Data
public class SprioProperties {

	String message;
}
