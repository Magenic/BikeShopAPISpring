package com.magenic.bikeshopapi.health;

import com.magenic.bikeshopapi.services.BicycleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HealthCheck implements HealthIndicator {

  private static final Logger LOG = LoggerFactory.getLogger(HealthCheck.class);

  private final BicycleService service;

  public HealthCheck(BicycleService service) {
    this.service = service;
  }

  @Override
  public Health health() {

    LOG.debug("Performing health check...");
    Health appHealth;

    HealthDetails details = performHealthCheck();

    if (details.getErrorCode() != 0) {
      appHealth = Health.down().withDetail("Error Code", details.getErrorCode())
          .withDetail("Error Message", details.getMessage()).build();
    }

    LOG.info("Finished health check: {}", details);

    appHealth = Health.up()
        .withDetail("Message", details.getMessage())
        .build();

    // TODO: Would be nice to map the response to an HTTP response here.
    return appHealth;
  }

  public HealthDetails performHealthCheck() {
    int errorCode = 0;
    String message;

    try {
      long bicycleCount = this.service.getBicycleCount();

      message = String.format("Connection to database successful. Found %s objects in database.", bicycleCount);

    } catch (Exception ex) {
      message = "Cannot connect to database. Check logs for more information.";
      errorCode = 1;
      LOG.error("Error performing health check.", ex);
    }

    return new HealthDetails(errorCode, message);
  }
}
