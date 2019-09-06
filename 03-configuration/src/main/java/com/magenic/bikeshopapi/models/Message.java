package com.magenic.bikeshopapi.models;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;


@RefreshScope
@Component
public class Message {

  private String title;
  private String body;

  public Message(@Value("${headerMessage.title:none}") String title, @Value("${headerMessage.body:none}") String body) {
    this.body = body;
    this.title = title;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @return the body
   */
  public String getBody() {
    return body;
  }

  @Override
  public String toString() {
    return String.format("Message(\"%s\", \"%s\")", this.title, this.body);
  }
}
