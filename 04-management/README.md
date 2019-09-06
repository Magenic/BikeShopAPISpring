# Management

**The purpose of this lab is to demonstrate how to use the management endpoints
that are included when you include the [Spring Actuator]() dependencies. Those
dependencies were covering briefly in the last lab, but in this lab you will
dive into the Spring Actuator endpoints in more detail.**

## Continuing from Configuration

This lab continues from the _Configuration_ lab, so you will want to make sure that
you have completed that lab before continuing onto this one.

### Add Dependencies

To include the Spring Actuator dependencies in your `build.gradle` file, include the
following dependencies:

```gradle
	implementation('org.springframework.boot:spring-boot-starter-actuator')
```

Simply including this dependency will turn on some default endpoints, such as a 
basic health endpoint. For security reasons, a number of the endpoints are not 
enabled by default, so follow the next steps if you would like to enable them.

---

### Update the Configuration

To enable or disable actuator endpoints, edit the `management.endpoints.web.exposure.include`
and `management.endpoints.web.exposure.exclude` configuration keys to include or
exclude endpoints, respectively.

A full list of the available endpoints can be found in the [Spring Boot Actuator Endpoints documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html).

To enable a few endpoints for the application, add the following to your `application.yml` file:

```yaml
management:
  endpoints:
    web:
      exposure:
        include:
          - health
          - info
          - metrics
```

You can test the application right now, as-is, by performing a `./gradlew clean bootJar` and a `cf push` of the 
application and navigating to `${BASE_URL}/actuator/`. As an example, the following command:

```bash
http bikeshop-wacky-oribi.cfapps.io/actuator/health
```

By default, you will see a result like the one shown here:

```json
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
Content-Length: 15
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Thu, 24 Jan 2019 20:32:02 GMT
Expires: 0
Pragma: no-cache
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vcap-Request-Id: ec88ffc2-638d-4233-7125-b177eea04ed2
X-Xss-Protection: 1; mode=block

{
    "status": "UP"
}
```

This is the result of the default health check. The Spring Actuator makes it easy for you to 
implement a custom health check.

For the custom health check, you will go back to the *BicycleService*
and *BicycleController* from the previous labs to implement more of the code
that will help you save and delete bicycles.

This is because you are going to eventually add custom, detailed information about
the health of the application that includes the number of bicycles in the repository,
and the additional endpoints will allow you to test it.

---

### Update the Bicycle Service

Add the *deleteBicycle* and *updateBicycle* methods to the *BicycleService*
as shown in the complete class listing here:

```java
@Component
public class BicycleService {

    private BicycleRepository repository;

    public BicycleService(BicycleRepository bicycleRepository) {
        this.repository = bicycleRepository;
    }

    public Iterable<Bicycle> getAllBicycles() {
        return repository.findAll();
    }

    public Optional<Bicycle> getBicycle(long id) {
        return repository.findById(id);
    }

    public Bicycle createBicycle(Bicycle bicycle) {
        return this.repository.save(bicycle);
    }

    public void deleteBicycle(long id) {
        this.repository.deleteById(id);
    }

    public Bicycle updateBicycle(Bicycle bicycle) {
        return this.repository.save(bicycle);
    }
}
```


---

### Update the Bicycle Controller

Add new methods to the *BicycleController* to support the HTTP 
DELETE and PUT endpoints, which in turn call the new methods on the *BicycleService*.

Modify the `BicycleController.java` file so that it looks like this:

```java
@RestController
public class BicycleController {

    private BicycleService service;

    /**
     * Creates an instance of the {@link BicycleController} class with the given {@link BicycleService}.
     * @param bicycleService
     */
    public BicycleController(BicycleService bicycleService) {
        this.service = bicycleService;
    }

    /**
     * Gets all of the bicycles in the repository.
     * @return
     */
    @GetMapping("/api/bicycle")
    public Iterable<Bicycle> get() {
        return this.service.getAllBicycles();
    }

    /**
     * Gets a single bicycle with the matching ID.
     *
     * @param id The identifier for the {@link Bicycle}
     * @return The matching {@link Bicycle}
     */
    @GetMapping("/api/bicycle/{id}")
    public Bicycle get(@PathVariable long id) {
        return this.service.getBicycle(id).orElseThrow(() -> new RuntimeException(String.format("Item with the ID %s was not found.", id)));
    }

    /**
     * Creates a new {@link Bicycle} object in the database.
     * @param bicycle
     * @return
     */
    @PostMapping("/api/bicycle")
    public Bicycle save(@RequestBody Bicycle bicycle) {
        return this.service.createBicycle(bicycle);
    }

    /**
     * Deletes the {@link Bicycle} from the database.
     * @param id Long identifier for the {@link Bicycle}
     * @return void
     */
    @DeleteMapping("/api/bicycle/{id}")
    public void delete(@PathVariable long id) {
        this.service.deleteBicycle(id);
    }

    /**
     * Updates the provided {@link Bicycle} in the database.
     * @param bicycle
     * @return
     */
    @PutMapping("/api/bicycle/")
    public Bicycle update(@RequestBody Bicycle bicycle) {
        return this.service.updateBicycle(bicycle);
    }
}
```

Now that you've added support for deleting and updating the objects in the
database, you can add a custom health indicator that will show you how many 
objects you have in your database.

---

### Create a Custom Health Check

Create a `HealthCheck.java` file in the `src/main/java/com/magenic/cloudnativelab/health`
folder with the following contents:

```java
@Component
public class HealthCheck implements HealthIndicator {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheck.class);

    private final BicycleService service;

    public HealthCheck(BicycleService service) {
        this.service = service;
    }

    @Override
    public Health health() {

        LOG.debug("Performing health check...");
        Health appHealth;

        HealthDetails details = performHealthCheck();

        if (details.getErrorCode() != 0) {
            appHealth = Health.down().withDetail("Error Code", details.getErrorCode())
                    .withDetail("Error Message", details.getMessage()).build();
        }

        LOG.info("Finished health check: {}", details);

        appHealth = Health.up()
            .withDetail("Message", details.getMessage())
            .build();

        // TODO: Would be nice to map the response to an HTTP response here.
        return appHealth;
    }

    public HealthDetails performHealthCheck() {
        int errorCode = 0;
        String message;

        try {
            long bicycleCount = this.service.getBicycleCount();

            message = String.format("Connection to database successful. Found %s objects in database.", bicycleCount);

        } catch (Exception ex) {
            message = "Cannot connect to database. Check logs for more information.";
            errorCode = 1;
            LOG.error("Error performing health check.", ex);
        }

        return new HealthDetails(errorCode, message);
    }
}
```

> Note: If you do some checking, you will see that the there are a few different
> methods for extending the Actuator endpoints with the @EndpointWebExtension 
> attribute. This approach will cause you some headaches in PCF because there is
> already an extension on the health endpoint, so following this approach will work.

---

### Update the Configuration

While the *HealthCheck* that you added will contribute to the overall status
right away, you will not see the details of the *Health* unless you make some
changes in the response.

Configure the health endpoint to show details by adding the following code 
in the `application.yml` file:

```yaml
management:
  endpoint:
    health:
      show-details: "ALWAYS"
  endpoints:
    web:
      exposure:
        include:
          - health
          - info
          - metrics
```

The `endpoints.web.exposure.include` configuration exposes the endpoints, so make sure
the `health` and the `info` endpoints are listed because you will be using both in this
lab. You can optionally list other endpoints, such as `metrics` and `env`, but keep in
mind that some endpoints (like `env`) expose details about your application that you 
do not want to expose until you have enabled security.

---

### Testing so far

At this point, you can test the `health` endpoint and see the results of the extension
that you added. To test what you have done, you will push the app to PCF and then 
call the health endpoint, which is `${BASE_URL}/actuator/health`.

1. Build your app:

    ```bash
    ./gradlew clean bootJar
    ```

1. Push your app to PCF:

    ```bash
    cf push
    ```

You can see the health output in the PCF console. In [AppsMan](https://apps.cf.magenic.net),
select the **BikeShop** app and go to the **Overview** tab. Under **Processes and Instances**,
expand your app and see the results of the health check there.

You can also use the command line to test the endpoint. Use the command:

```bash
http ${BASE_URL}/actuator/health
```

Without enabling the `management.endpoint.health.show-details` configuration, you will see
the output below. Your health check still contributes to the overall *UP* or *DOWN* status,
but the detailed messages aren't displayed.

```json
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
Content-Length: 103
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Fri, 25 Jan 2019 21:07:39 GMT
Expires: 0
Pragma: no-cache
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vcap-Request-Id: 1fb081e1-b237-4d80-5a6b-0653f8dfee07
X-Xss-Protection: 1; mode=block
{
    "status": "UP
}
```

After you have turned on how details, you can see the detailed message. You can also see all
of the components that contribute to the overall cloud health check, such as connection to the
data store and more. You can call your health check endpoint directly by using the URL 
`${BASE_URL}/actuator/health/healthCheck`. 

The output of calling your custom health check will look like this:

```bash
http ${BASE_URL}/actuator/health/healthCheck
```

```json
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
Content-Length: 103
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Sat, 26 Jan 2019 16:18:53 GMT
Expires: 0
Pragma: no-cache
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vcap-Request-Id: 5062920f-1116-454f-44dc-eff8610b7d94
X-Xss-Protection: 1; mode=block

{
    "details": {
        "Message": "Connection to database successful. Found 2 objects in database."
    },
    "status": "UP"
}
```

At this point you can call the add, delete, and update methods on your controller and see the
count change.

> Note: The name of the component in the last segment in the URI is derived from the name
> of the class, in camel case rather than Pascal case, so if you named your class something
> else you will have a different endpoint.

Now that you have added a custom health endpoint, you can add more customizations to
different endpoints, like `/info`, to provide more information about the app.

---

## Add Info

An `/info` endpoint can display various information about your application, such as the 
name, version information, and more. 

You can add information to the `/info` endpoint by either adding values in the 
configuration or by implementing the *InfoContributor* interface. 

### Using Configuration to Expose Info

For example, to expose a name, description, and version number for the application, 
add the following values to the `application.yml` file:

```yaml
info:
  app:
    name: BikeShop Java API
    description: Java REST API for the BikeShop app.
    version: 0.0.1-SNAPSHOT
  java-vendor: ${java.specification.vendor}
```

There are plugins available for both Maven and Gradle that automatically update these 
values with useful information like the git commit hash.

After you add the configuration above, re-build and re-push the application using the
same steps as before. To test, navigate to the `/info` endpoint:

```bash
http ${BASEURL}/actuator/info
```

```json
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
Content-Length: 150
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Sat, 26 Jan 2019 16:58:38 GMT
Expires: 0
Pragma: no-cache
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vcap-Request-Id: 3542b765-ce9b-4340-7521-0748927eff92
X-Xss-Protection: 1; mode=block

{
    "app": {
        "description": "Java REST API for the BikeShop app.",
        "name": "BikeShop Java API",
        "version": "0.0.1-SNAPSHOT"
    },
    "java-vendor": "Oracle Corporation"
}
```

> Note: there are ways to use Maven or Gradle to expand the project properties in
> the `application.yml` file so that you don't have to worry about having the
> information in two places to get out of sync. See more in the *Resources* section.

### Create a Info Contributor

To use code to contribute to the `/actuator/info` endpoint, create the
`BicycleInfoContributor.java` file in the `src/main/java/com/magenic/cloudnativelab/health` 
directory with the following contents:

```java
@Component
public class BicycleInfoContributor implements InfoContributor {

    BicycleService service;

    public BicycleInfoContributor(BicycleService bicycleService) {
        this.service = bicycleService;
    }

    @Override
    public void contribute(Builder builder) {
        long bicycleCount = this.service.getBicycleCount();

        builder.withDetail("bicycleInfo", 
            Collections.singletonMap(
                "count", bicycleCount)
        );
    }

}
```

After you add the custom implementation of the *InfoContributor*, you can test the new
info by re-building and re-pushing your application to PCF and calling the `/actuator/info`
endpoint again:

```bash
http ${BASE_URL}/actuator/info/
```

```json
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
Content-Length: 167
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Sat, 26 Jan 2019 18:02:12 GMT
Expires: 0
Pragma: no-cache
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vcap-Request-Id: 6c6d2853-6649-49d7-4997-379063eed08c
X-Xss-Protection: 1; mode=block

{
    "app": {
        "description": "Java REST API for the BikeShop app.",
        "name": "BikeShop",
        "version": "0.0.1-SNAPSHOT"
    },
    "bicycleInfo": {
        "count": 2
    },
    "java-vendor": "Oracle Corporation"
}
```

You should see now the `count`. As you change the count by calling your new create
and delete methods on the *BicycleController*.

## Add Metrics

> Note: In the C# .NET version of this lab, a you would create custom operation 
> tracker to track the CRUD operations with bicycles. While every effort has
> been made in the Java/Spring version of this lab, there are some cases where
> a different approach might be used because of different libraries available.
> Metrics on controller operations is one of those cases--using the new 
> [Micrometer]() in Spring 2.x+, there is already a mechanism for capturing 
> controller events. This example will cover that mechanism rather than creating
> custom classes.

To add metrics to your endpoints, such as to capture the time that the API spends
listing all of your bicycles and to understand the average timing and maximum time,
use the `@Timed` attribute to annotate the methods as shown here:

```java
@Timed
@RestController
public class BicycleController {

    private BicycleService service;

    public BicycleController(BicycleService bicycleService) {
        this.service = bicycleService;
    }

    @Timed(value = "all.bicycles")
    @GetMapping("/api/bicycle")
    public Iterable<Bicycle> get() {
        return this.service.getAllBicycles();
    }

    @Timed(value = "get.bicycle")
    @GetMapping("/api/bicycle/{id}")
    public Bicycle get(@PathVariable long id) {
        return this.service.getBicycle(id).orElseThrow(() -> new RuntimeException(String.format("Item with the ID %s was not found.", id)));
    }

    @Timed(value = "new.bicycle")
    @PostMapping("/api/bicycle")
    public Bicycle save(@RequestBody Bicycle bicycle) {
        return this.service.createBicycle(bicycle);
    }

    @Timed(value = "delete.bicycle")
    @DeleteMapping("/api/bicycle/{id}")
    public void delete(@PathVariable long id) {
        this.service.deleteBicycle(id);
    }

    @Timed(value = "update.bicycle")
    @PutMapping("/api/bicycle/")
    public Bicycle update(@RequestBody Bicycle bicycle) {
        return this.service.updateBicycle(bicycle);
    }
}
```

To test, rebuild the application using `./gradlew bootJar` and push it to PCF using 
`cf push`, and you will be able to use the names you provided as paths after the `/actuator/metrics`
endpoint to get more information about any one of them. For example, running the 
following command:

```bash
http ${BASE_URL}/actuator/metrics/all.bicycles
```

Will return the following result:

```json
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
Content-Disposition: inline;filename=f.txt
Content-Length: 404
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Mon, 28 Jan 2019 16:02:45 GMT
Expires: 0
Pragma: no-cache
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vcap-Request-Id: a32938cc-4bad-464d-4fd1-c39b4ff84394
X-Xss-Protection: 1; mode=block

{
    "availableTags": [
        {
            "tag": "exception",
            "values": [
                "None"
            ]
        },
        {
            "tag": "method",
            "values": [
                "GET"
            ]
        },
        {
            "tag": "uri",
            "values": [
                "/api/bicycle"
            ]
        },
        {
            "tag": "outcome",
            "values": [
                "SUCCESS"
            ]
        },
        {
            "tag": "status",
            "values": [
                "200"
            ]
        }
    ],
    "baseUnit": "seconds",
    "description": null,
    "measurements": [
        {
            "statistic": "COUNT",
            "value": 1.0
        },
        {
            "statistic": "TOTAL_TIME",
            "value": 0.417589169
        },
        {
            "statistic": "MAX",
            "value": 0.417589169
        }
    ],
    "name": "all.bicycles"
}
```

You can add timings to any other endpoints that you would like to capture.


## Create a Custom Endpoint

In addition to custom health checks, contributing to information, and built-in metrids, you 
can also create completely custom endpoints for displaying information about your application.

An example might be displaying custom usage information that you can't capture easily
with the other built-in metrics.

To create your own custom actuator endpoint, create a `MyCustomEndpoint.java` file in the
`src/main/java/com/magenic/cloudnativelab/health` directory with the following contents:

```java
@Controller
@Endpoint(id="my-endpoint")
public class MyCustomEndpoint {

    private final static String MY_CUSTOM_INFO = "Some information about my application";

    @ReadOperation()
    public String getInformation() {
        return MY_CUSTOM_INFO;
    }
}
```

You will need to turn on the endpoint in the `application.yml` file by adding the ID value
to the list of enabled endpoints as shown here:

```yaml
management:
  endpoint:
    health:
      show-details: "ALWAYS"
  endpoints:
    web:
      exposure:
        include:
          - health
          - info
          - metrics
          - my-endpoint
```

Once you are finished creating the *MyCustomEndpoint* class and enabling the endpoint in the
configuration, re-build the application and re-push it to PCF to see it in action.

Once you have pushed the application to PCF, you will now see your new endpoint in the list
of those returned by the `/actuators/` endpoint.

Now call the endpoint:

```bash
http ${BASE_URL}/actuator/my-endpoint
```

You will get the following response:

```
HTTP/1.1 200 OK
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Connection: keep-alive
Content-Length: 37
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8
Date: Mon, 28 Jan 2019 17:19:42 GMT
Expires: 0
Pragma: no-cache
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-Vcap-Request-Id: b9150673-2cff-4113-7044-d53dd9b273b2
X-Xss-Protection: 1; mode=block

Some information about my application
```


This is a very simple example that demonstrates how you can write your own custom endpoints
that are exposed by the Spring Actuator.

---

## Resources

* Read about https://www.baeldung.com/spring-boot-actuators.
* See the list of endpoints at https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html