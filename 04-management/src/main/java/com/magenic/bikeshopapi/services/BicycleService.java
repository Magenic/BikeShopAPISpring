package com.magenic.bikeshopapi.services;

import com.magenic.bikeshopapi.models.Bicycle;
import com.magenic.bikeshopapi.repositories.BicycleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
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

  public void deleteBicycle(long id) {
    this.repository.deleteById(id);
  }

  public Bicycle updateBicycle(Bicycle bicycle) {
    return this.repository.save(bicycle);
  }

  public long getBicycleCount() {
    return this.repository.count();
  }
}