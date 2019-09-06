package com.magenic.bikeshopapi.controllers;

import com.magenic.bikeshopapi.models.Bicycle;
import com.magenic.bikeshopapi.services.BicycleService;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for the {@link Bicycle}.
 */
@RestController
public class BicycleController {

  private BicycleService service;

  /**
   * Creates an instance of the {@link BicycleController} class with the given
   * {@link BicycleService}.
   *
   * @param bicycleService {@link BicycleService} implementation.
   */
  public BicycleController(BicycleService bicycleService) {
    this.service = bicycleService;
  }

  /**
   * Gets all of the bicycles in the repository.
   *
   * @return List of {@link Bicycle} objects.
   */
  @GetMapping("/api/bicycle")
  public Iterable<Bicycle> get() {
    return this.service.getAllBicycles();
  }

  /**
   * Gets a single bicycle with the matching ID.
   *
   * @param id The identifier for the {@link Bicycle}.
   * @return The matching {@link Bicycle}.
   */
  @GetMapping("/api/bicycle/{id}")
  public Bicycle get(@PathVariable long id) {
    return this.service.getBicycle(id)
        .orElseThrow(() ->
            new RuntimeException(String.format("Item with the ID %s was not found.", id)));
  }

  /**
   * Creates a new {@link Bicycle} object in the database.
   *
   * @param bicycle The {@link Bicycle} to add.
   * @return The newly-added {@link Bicycle}.
   */
  @PostMapping("/api/bicycle")
  public Bicycle post(@RequestBody Bicycle bicycle) {
    return this.service.createBicycle(bicycle);
  }

}
