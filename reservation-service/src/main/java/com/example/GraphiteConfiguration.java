package com.example;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import lombok.Data;

//@Configuration
@EnableConfigurationProperties(GraphiteProperties.class)
public class GraphiteConfiguration {

	@Bean
	GraphiteReporter graphiteReporter(MetricRegistry registry, GraphiteProperties properties) {
		GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
				.prefixedWith("reservations")
				.build(new Graphite(properties.host, properties.port));
		reporter.start(2, TimeUnit.SECONDS);
		return reporter;
	}

}

@ConfigurationProperties(prefix = "graphite")
@Data
class GraphiteProperties {

	String host;

	int port;
}