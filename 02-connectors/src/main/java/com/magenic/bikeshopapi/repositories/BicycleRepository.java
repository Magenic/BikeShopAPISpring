package com.magenic.bikeshopapi.repositories;

import com.magenic.bikeshopapi.models.Bicycle;
import org.springframework.data.repository.CrudRepository;

/**
 * Spring data repository for handling {@link Bicycle} objects.
 */
public interface BicycleRepository extends CrudRepository<Bicycle, Long> {
}