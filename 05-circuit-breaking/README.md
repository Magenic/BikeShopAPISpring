# Circuit-Breaking

*The purpose of this lab is to demonstrate how to implement a circuit breaker that will
allow your application to gracefully fall back in the event of errors.*

This lab continues from the *Management* lab, so you will want to make sure that you have completed that lab before continuing onto this one.

## Add Dependencies

To include the Netflix Hystrix dependencies in your `build.gradle` file, include the 
following dependencies:

```gradle
	compile('org.springframework.cloud:spring-cloud-starter-netflix-hystrix')
```

Simply including this dependency will turn on the Netflix Hystrix circuit breaker,
which provides basic fallback functionality, among other things.

## Create a MesssageService

In the *Configuration* lab, you created a *MessageController* that used the configured
*Message* to disply some values. In this circuit breaker example, you will create
a *MessageService* and do a little refactoring. You will throw an exception, on 
purpose, and see how the circuit breaker elegantly falls back to another 
method.

Create the `MessageService.java` file in the `src/main/java/com/magenic/cloudnativelab/services`
directory with the contents as shown here:

```java
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
        return new MessageBuilder()
            .setTitle(message.getTitle())
            .setBody(message.getBody())
            .build();
        throw new RuntimeException("Failure!!!!!");
    }

    @HystrixCommand
    public Message fallbackMessage() {
       return FALLBACK_MESSAGE; 
    }
}
```

There are a couple things to note about the service code. First, the *getMessage* 
method is marked with the `@HystrixCommand` attribute.

After creating this new service, you will refactor the *MessageController* to use it 
instead of the Message directly.

## Refactor the MessageController

Refactor the *MessageController* 
(`src/main/java/com/magenic/cloudnativelab/controllers/MessageController.java`) 
to use the *MessageService* as shown here:

```java
@RestController
public class MessageController {

    private MessageService service;

    MessageController(MessageService messageService) {
        this.service = messageService;
    }

    @GetMapping("/api/message")
    public Message getMessage() {
        return this.service.getMessage();
    }
}
```

Now that you have the controller and service updated, you can re-build the 
project using `./gradlew clean bootJar` and re-push the project to PCF using
`cf push`. 

When you call the enpoint, `/api/message`, you should see the same message as before.

## Watch the Circuit Breaker in Action

Now you can add some code to the service that will throw an exception on purpose.
This is a little contrived, but throwing the exception will help you easily 
simulate something going wrong with the service call. 

Modify the *MessageService* class so that it looks like this:

```java
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
```

The differences here are the additions of the `@HystrixCommand`, specifying the 
*fallbackMessage* method as a `fallbackMethod` that will get executed when something
goes wrong with the original *getMessage*.

When the exception is thrown, the circuit breaker will "trip" and will fall back
to the method that you configured in the `@HystrixCommand` annotation. 

> Note: This is _not_ a pattern for handling exceptions per se, so avoid using 
> it as one. Use this in a case when there is a reasonable default or a reasonable
> message from a cache or something if the data source becomes unavailable. 

Now that you have added the circuit breaker code, re-build the project using
`./gradlew clean bootJar` and push it to PCF using `cf push`. 

Now you can call the original endpoint, but you will see the output of the
fallback message.

```bash
http ${BASE_URL}/api/message
```

```json
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
Content-Length: 65
Content-Type: application/json;charset=UTF-8
Date: Tue, 29 Jan 2019 17:36:54 GMT
Expires: 0
Pragma: no-cache
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vcap-Request-Id: 800da26e-a0fe-4dcc-5d6a-62fde128c111
X-Xss-Protection: 1; mode=block

{
    "body": "This is a fallback description",
    "title": "Default title"
}
```

You can see that the default message is returned. If you remove the code that
throws the exception and re-deploy the code to PCF, you will see the value
switch back to normal.

## Resources

For more information about the Circuit Breaker pattern and how to use it in Spring,
see the links below.

* Read [Circuit Breaker: Getting Started](https://spring.io/guides/gs/circuit-breaker/).
