package com.magenic.bikeshopapi.services;

import com.magenic.bikeshopapi.models.Message;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class MessageService {

  private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

  private static final Message FALLBACK_MESSAGE = new Message("Default title", "This is a fallback description");

  private Message message;

  public MessageService(Message message) {
    LOG.info(String.format("Initializing with message: %s", message));
    this.message = message;
  }

  @HystrixCommand(fallbackMethod = "fallbackMessage")
  public Message getMessage() {
    LOG.info(String.format("Returning message with values: %s", message.getTitle()));
    // This could return the following code, but instead it will deliberately
    // throw a RuntimeException to simulate something being unreachable...
    //
    // return new MessageBuilder()
    //     .setTitle(message.getTitle())
    //     .setBody(message.getBody())
    //     .build();
    throw new RuntimeException("Failure!!!!!");
  }

  @HystrixCommand
  public Message fallbackMessage() {
    return FALLBACK_MESSAGE;
  }
}