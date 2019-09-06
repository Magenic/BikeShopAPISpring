package com.magenic.bikeshopapi.health;

import com.magenic.bikeshopapi.services.BicycleService;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class BicycleInfoContributor implements InfoContributor {

  BicycleService service;

  public BicycleInfoContributor(BicycleService bicycleService) {
    this.service = bicycleService;
  }

  @Override
  public void contribute(Builder builder) {
    long bicycleCount = this.service.getBicycleCount();

    builder.withDetail("bicycleInfo",
        Collections.singletonMap(
            "count", bicycleCount)
    );
  }

}