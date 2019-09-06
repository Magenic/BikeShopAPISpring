package com.magenic.bikeshopapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Bicycle {
  @Id
  @GeneratedValue
  private long id;
  private String productName;
  private double price;
  private String description;
  private String image;
}