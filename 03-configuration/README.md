# Configuration

**The purpose of this lab is to demonstrate how to use a configuration service
in PCF. Using a configuration service enables you to externalize your configuration,
which is one of the factors in [The Twelve-Factor App](https://12factor.net/).**

## Continue from Connectors

This lab continues from the _Connectors_ lab, so you will want to make sure that 
you have completed that lab before continuing onto this one.

### Open BikeShop

1. Open the solution folder `~/Workspace/cloud-native-lab` in your favorite
Integrated Development Environment (IDE).

2. Open a terminal and change directories.

    ```bash
    cd ~/Workspace/cloud-native-lab
    ```

> Note: To view the code for this lab, view the `Configuration` branch in the source
> code repository.

---

## Add Dependencies

There are some dependencies that, when added to Spring, will assist in automatically configuring the configuration server for Spring in Pivotal Cloud Foundry (PCF).

Make sure the `dependencies` section looks like the following in your `build.gradle` file. You will 
also add a `dependencyManagement` section as shown here.

```gradle
dependencyManagement {
	imports {
		mavenBom "org.springframework.cloud:spring-cloud-dependencies:Finchley.RELEASE"
        mavenBom "io.pivotal.spring.cloud:spring-cloud-services-dependencies:2.0.3.RELEASE"
	}
}

dependencies {
	compileOnly('org.projectlombok:lombok')
	implementation 'org.apache.httpcomponents:httpclient:4.5.6'
	implementation('org.springframework.boot:spring-boot-starter-actuator')
	implementation('org.springframework.boot:spring-boot-starter-cloud-connectors')
	implementation('org.springframework.boot:spring-boot-starter-data-jpa')
	implementation('org.springframework.boot:spring-boot-starter-web')
	implementation('org.springframework.cloud:spring-cloud-starter-config')
	runtimeOnly('mysql:mysql-connector-java')
	runtimeOnly('com.h2database:h2')
	testImplementation('org.springframework.boot:spring-boot-starter-test')
}
```

---

### Add a bootstrap.yml File

The `bootstrap.yml` file includes the name of the Spring boot application.
The name of the application is important because it will be used to load the 
configuration values from the configuration service later.

Add the following content to the `bootstrap.yml` file:

```yaml
spring:
  application:
    name: BikeShop
```

---
### Optional: Update the application.yml File

> Note: the following step is optional for debugging. You can proceed to the 
> next section if you do not want to enable the `/actuator` endpoints.

To help out with debugging, you can expose some endpoints using 
[Actuator](https://www.baeldung.com/spring-boot-actuators). 
Some of the endpoints need to be explicitly enabled, so modify the 
`application.yml` file to include the following section:

```yaml
management:
  endpoints:
    web:
      exposure:
        include:
          - health
          - info
          - env
          - metrics
```

After you have enabled the endpoints, you can navigate to `${BASE_URL}/actuator` in
your application to see the list of endpoints available to you. To view the 
current environment variables and configuration, as an example, navigate to
`${BASE_URL}/actuator/env`. You will see a JSON structure that describes all of the
configuration values and their sources, which is very useful in this lab. 

> **Important: the Spring Actuator endpoints can expose some things you may not
> want to expose, so think twice about turning anything on in an insecure 
> environment.**

---

### Create the Configuration Server Configuration file

Now create a new instance of a configuration service in PCF. The configuration
service is based on TODO. The configuration service can use several different
backends for configuration--one of them is a Git backend. To demonstrate how
the Git backend works, you will create an instance of the configuration 
service in PCF with a Git repository URL.

A JSON file can be used when creating the service to configure the git 
backend. Create a new file called `configuration-server.json` with the following contents:


```json
{
  "git" : {
    "uri": "https://github.com/robertsirc/Cloud-Native-Configs.git"
  }
}
```

Once you have created this file, you can now use it to configure a new instance of the 
config server in PCF.

> Note: If you take a look at this repository, you will find a file in the repository called
> `BikeShop.yml`. It is important that the name of the application that you set in the 
> `bootstrap.yml` file matches up with the name of the file here (and it is case sensitive)! 
> In other words, the configuation server will load the file that is called 
> `[APPLICATION_NAME].yml`, where *[APPLICATION_NAME]* is the name of the application
> set in `bootstrap.yml`. I use the same name in the `manifest.yml` file as well.

### Create the Configuration Server

To create a new instance of the configuration server in PCF, run the following command:

```bash
cf create-service p-config-server standard BikeShopConfigServer -c /PATH/TO/configuration-server.json
```

Where `/PATH/TO` is the full path to the JSON configuration file that you created in the previous
step.

---

### Update the Manifest file

To use the new configuration service, you will need to bind the application to it. 

Modify the `manifest.yml` file to include the new service under the `services` tag. Additionally,
for security reasons (see "Add Security Configuration"), add the *development* profile to the
list of profiles as shown below.

```yml
applications:
- name: BikeShop
  disk_quota: 1G
  instances: 1
  memory: 728M
  random-route: true
  stack: cflinuxfs2
  path: build/libs/cloud-native-lab-0.0.1-SNAPSHOT.jar
  env:
    SPRING_PROFILES_ACTIVE: cloud,mysql,development
  services:
    - mysql
    - BikeShopConfigServer
```

Now that you have added the new service and new Spring profile to the `manifest.yml` file,
you are ready to configure security for development and then push the project to PCF.

### Add Security Configuration

When you add the new Spring cloud starter dependencies, configuration will automatically
turn on for the application. As security is not enabled throughout the application yet,
you will want to create a configuration--for the *development* profile only--that 
disables Spring security.

Do add the configuration, create the `SecurityConfig.java` file in the 
`src/main/java/com/magenic/cloudnativelab` with the contents shown here:

```java
@Configuration
@Profile("development")
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * This method disables the security.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().anyRequest().permitAll()
            .and()
            .httpBasic().disable()
            .csrf().disable();
    }
}
```

### Create a Message Model

In the `models` directory, create a new file called `Message.java` with the 
following contents:

```java
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
```

When the Spring framework loads this object, because it is marked as a `@Component`
and uses `@Value` to set the values in the constructor, Spring will inject
the configuration values into the object.

Now that you have created a model object to hold the configuration values, you can
create a controller that will be used to return the values when you call the
correct endpoint.

---

### Create a Message Controller

In the `controllers` directory create a new controller called `MessageController.java` 
with the following contents:

```java
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
```

You will note that there is a `MessageBuilder` class here that builds a new copy of
the configured message object. This isn't strictly necessary, but provides an example
of how you might map to a different object for the response here if you didn't want
to return the `Message` object directly. 

---
## Push BikeShop to PCF

1. Build the project with Gradle using the command `./gradlew clean bootJar`. This 
command will clean the project and create a new JAR that can be pushed to PCF.

1. Push the application to PCF

    ```bash
    cf push
    ```

---

### Testing BikeShop API

To test the service, you will need the base URL of your application. Find out the base URL by using the command `cf apps` and looking under urls for the app.

Once you have the base URL, test the `/api/message` API by navigating to in with your browser or by typing the command:

```sh
http ${BASE_URL}/api/message
```

Where *${BASE_URL}* is the URL route for your app. 

You should see a result like this:

```json
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
Content-Length: 100
Content-Type: application/json;charset=UTF-8
Date: Wed, 23 Jan 2019 20:36:47 GMT
Expires: 0
Pragma: no-cache
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vcap-Request-Id: fc2d1528-046f-4bd1-5a3f-86b6214be2a4
X-Xss-Protection: 1; mode=block

{
    "body": "Great bike sale this week on select bikes and accessories.",
    "title": "Bike Sale this week!"
}
```

---

## Recap

Continuing from the previous lab, you have added code to use values from a configuration server. 
You wrote a new controller and new message object that exposes the values.

## Next Steps
Next, you will go through some of the available management endpoints that can be tapped to produce real time data about your apps operation. Change to the Management branch to continue.

## Resources

Use the list of resources below to learn more about configuration using Spring in PCF.

* Read more about [Writing Client Applications](https://docs.pivotal.io/spring-cloud-services/2-0/common/config-server/writing-client-applications.html#use-configuration-values) from the Pivotal documentation.
* Read more about [Configuring with Git](https://docs.pivotal.io/spring-cloud-services/2-0/common/config-server/configuring-with-git.html) to learn how to configure to particular git labels.
* Read the [Spring Cloud Config](http://cloud.spring.io/spring-cloud-config/single/spring-cloud-config.html) documentation, on which the PCF configuration server is based.
* Read the [Spring Cloud Config Quickstart](http://cloud.spring.io/spring-cloud-config/multi/multi__quick_start.html#_client_side_usage).
* See an example config git repository at [https://github.com/spring-cloud-services-samples/cook-config](https://github.com/spring-cloud-services-samples/cook-config).
* See a sample application in the [Cook repository](https://github.com/spring-cloud-services-samples/cook).