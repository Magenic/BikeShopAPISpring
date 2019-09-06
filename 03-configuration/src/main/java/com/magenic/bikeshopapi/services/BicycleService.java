package com.magenic.bikeshopapi.services;

import com.magenic.bikeshopapi.models.Bicycle;
import com.magenic.bikeshopapi.repositories.BicycleRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BicycleService {

  private BicycleRepository repository;

  public BicycleService(BicycleRepository bicycleRepository) {
    this.repository = bicycleRepository;
  }

  public Iterable<Bicycle> getAllBicycles() {
    return repository.findAll();
  }

  public Optional<Bicycle> getBicycle(long id) {
    return repository.findById(id);
  }

  public Bicycle createBicycle(Bicycle bicycle) {
    return this.repository.save(bicycle);
  }
}