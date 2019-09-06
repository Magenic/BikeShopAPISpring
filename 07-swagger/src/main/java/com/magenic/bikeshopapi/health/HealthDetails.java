package com.magenic.bikeshopapi.health;

class HealthDetails {

  private int errorCode;
  private String message;

  public HealthDetails(int errorCode, String message) {
    this.errorCode = errorCode;
    this.message = message;
  }

  public int getErrorCode() {
    return this.errorCode;
  }

  public String getMessage() {
    return this.message;
  }

  @Override
  public String toString() {
    return String.format(
        "Error code: %s; Details: %s",
        this.errorCode,
        this.message
    );
  }

}
