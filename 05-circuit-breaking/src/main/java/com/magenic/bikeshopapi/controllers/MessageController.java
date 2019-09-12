package com.magenic.bikeshopapi.controllers;

import com.magenic.bikeshopapi.models.Message;
import com.magenic.bikeshopapi.services.MessageService;

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

  private MessageService messageService;

  MessageController(MessageService messageService) {
    this.messageService = messageService;
  }

  @GetMapping("/api/message")
  public Message getMessage() {
    return this.messageService.getMessage();
  }
}