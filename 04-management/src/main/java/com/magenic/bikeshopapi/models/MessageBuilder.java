package com.magenic.bikeshopapi.models;

public class MessageBuilder {

  private String title;
  private String body;

  public MessageBuilder setTitle(String value) {
    this.title = value;
    return this;
  }

  public MessageBuilder setBody(String value) {
    this.body = value;
    return this;
  }

  public Message build() {
    return new Message(this.title, this.body);
  }
}
