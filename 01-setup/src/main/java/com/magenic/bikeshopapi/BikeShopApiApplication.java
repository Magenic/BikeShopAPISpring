package com.magenic.bikeshopapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application class.
 */
// CHECKSTYLE:OFF
@SpringBootApplication
// CHECKSTYLE:ON
public class BikeShopApiApplication {

  /**
   * Main entry point for the Spring Boot application.
   *
   * @param args Arguments to the application.
   */
  public static void main(final String[] args) {
    SpringApplication.run(BikeShopApiApplication.class, args);
  }
}
