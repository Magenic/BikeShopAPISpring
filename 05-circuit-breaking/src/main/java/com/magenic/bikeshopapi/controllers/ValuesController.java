package com.magenic.bikeshopapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * This is a basic example controller that was created to imitate the sample
 * contoller in the DotNet folder.
 */
@RestController
public class ValuesController {

  /**
   * List of seed values.
   */
  private static final List<String> VALUES = Arrays.asList(
      new String[]{"value1", "value2"});

  /**
   * Returns the full list of values.
   *
   * @return A {@see List} of {@see #VALUES}.
   */
  @GetMapping("/api/values")
  public ResponseEntity<List<String>> getValues() {
    return new ResponseEntity<>(VALUES, HttpStatus.OK);
  }

  /**
   * Returns the specific value with the given ID.
   *
   * @param id Identifier for the value.
   * @return The updated value.
   */
  @GetMapping("/api/values/{id}")
  public ResponseEntity<String> getValue(@PathVariable final String id) {
    return new ResponseEntity<>("value", HttpStatus.OK);
  }

  /**
   * Creates the value.
   *
   * @param value The value.
   * @return The value.
   */
  @PostMapping("api/values")
  public ResponseEntity<String> postValue(@RequestBody final String value) {
    return new ResponseEntity<>(value, HttpStatus.CREATED);
  }

  /**
   * Updates the given value at the ID.
   *
   * @param id    Identifier for the value.
   * @param value The value.
   * @return Returns {@see HttpStatus#OK} if the operation was successful.
   */
  @PutMapping("/api/values/{id}")
  public ResponseEntity<String> putValue(@PathVariable final String id,
                                         @RequestBody final String value) {
    return new ResponseEntity<>(value, HttpStatus.OK);
  }

  /**
   * Deletes the given value.
   *
   * @param id Identifier of the value.
   * @return Returns {@link HttpStatus#NO_CONTENT}
   */
  @DeleteMapping("/api/values/{id}")
  public ResponseEntity<Void> deleteValue(@PathVariable final String id) {
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

}
