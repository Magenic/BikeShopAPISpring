package com.magenic.bikeshopapi.health;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Controller;

@Controller
@Endpoint(id = "my-endpoint")
public class MyCustomEndpoint {

  private final static String MY_CUSTOM_INFO = "Some information about my application";

  @ReadOperation()
  public String getInformation() {
    return MY_CUSTOM_INFO;
  }
}