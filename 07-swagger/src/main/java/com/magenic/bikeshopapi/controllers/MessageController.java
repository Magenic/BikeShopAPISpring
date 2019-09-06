package com.magenic.bikeshopapi.controllers;

import com.magenic.bikeshopapi.models.Message;
import com.magenic.bikeshopapi.models.MessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 *
 */
@RestController
public class MessageController {

  private static final Logger LOG = LoggerFactory.getLogger(MessageController.class);

  private Message message;

  MessageController(Message message) {
    LOG.info(String.format("Initializing with message: %s", message));
    this.message = message;
  }

  @GetMapping("/api/message")
  public Message getMessage() {
    LOG.info(String.format("Returning message with values: %s", message.getTitle()));
    return new MessageBuilder()
        .setTitle(message.getTitle())
        .setBody(message.getBody())
        .build();
  }


}