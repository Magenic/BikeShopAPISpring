package com.magenic.bikeshopapi;

import com.magenic.bikeshopapi.models.Bicycle;
import com.magenic.bikeshopapi.repositories.BicycleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Class that includes configuration for a {@link CommandLineRunner} that executes
 * upon startup.
 */
@Configuration
@Slf4j
class BicycleDbInitialize {

  /**
   * Initializes the database with a few objects.
   *
   * @param repository {@link BicycleRepository} implementation.
   * @return {@see CommandLineRunner}
   */
  @Bean
  CommandLineRunner initDatabase(final BicycleRepository repository) {
    return args -> {
      log.info("Preloading "
          + repository.save(
          new Bicycle(1,
              "Schwinn Mountain Bike",
              899.99,
              "Schwinn",
              "./assets/images/bike5.jpg")
      ));
      log.info("Preloading "
          + repository.save(
          new Bicycle(2,
              "Nishiki Dirt Bike",
              399.99,
              "Nishiki",
              "./assets/images/bike6.jpg")
      ));
    };
  }
}
