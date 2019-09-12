package com.magenic.bikeshopapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;

/**
 * Spring Boot application class.
 */
@SpringBootApplication
@EnableCircuitBreaker
public class BikeShopApiApplication {

  /**
   * Main entry point for the Spring Boot application.
   */
  public static void main(String[] args) {
    SpringApplication.run(BikeShopApiApplication.class, args);
  }
}
