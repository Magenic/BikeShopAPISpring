# Setup

The objective of this lab is to demonstrate a basic project with only a minimal, basic
controller similar to the _ValuesController_ in a brand new .NET API project.

When complete, you will have a simple REST API with the endpoint `/api/values` that
you can view both locally and deployed to Pivotal Cloud Foundry (PCF)!

## Logging in to PCF

To use the `cf` command to push your new application to PCF, use the `cf login`
command as shown here:

``` bash
$ cf login -a <PCF API URL> --skip-ssl-validation
API endpoint: <PCF API URL>

Email> <YOUR EMAIL>

Password>
Authenticating...
OK

Targeted org <YOUR ORG NAME>

Targeted space <YOUR SPACE NAME>



API endpoint:   <YOUR API> (API version: 2.139.0)
User:           <YOUR EMAIL>
Org:            <YOUR ORG NAME>
Space:          <YOUR SPACE NAME>
```

Now that you have verified your login to PCF, you are ready to create your
new Spring Boot-based REST API and deploy it!

## Create Spring Boot Application

1. Generate a new Spring Boot project called "BikeShop".

     ``` bash
     $ http 'https://start.spring.io/starter.zip?type=gradle-project&language=java&bootVersion=2.1.7.RELEASE&baseDir=bike-shop-api-spring&groupId=com.magenic&artifactId=bike-shop-api-spring&name=bike-shop-api-spring&description=This+is+the+BikeShop+REST+API+implemented+in+SpringpackageName=com.magenic.bikeshopapi&packaging=jar&javaVersion=11&dependenicies=web,actuator' > project.zip
     $ unzip project.zip
     $ cd bike-shop-api-spring
     ```

2. Create a simple web controller with the contents shown here and save to `src/main/java/com/magenic/bikeshopapi/controllers/ValuesController.java`:

     ``` java
     package com.magenic.bikeshopapi.controllers;

     import java.util.Arrays;
     import java.util.List;

     import org.springframework.http.HttpStatus;
     import org.springframework.http.ResponseEntity;
     import org.springframework.web.bind.annotation.DeleteMapping;
     import org.springframework.web.bind.annotation.GetMapping;
     import org.springframework.web.bind.annotation.PathVariable;
     import org.springframework.web.bind.annotation.PostMapping;
     import org.springframework.web.bind.annotation.PutMapping;
     import org.springframework.web.bind.annotation.RequestBody;
     import org.springframework.web.bind.annotation.RestController;

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
          new String[] {"value1", "value2"});

     /**
     * Returns the full list of values.
     * @return A {@see List} of {@see #VALUES}.
     */
     @GetMapping("/api/values")
     public ResponseEntity<List<String>> getValues() {
     return new ResponseEntity<>(VALUES, HttpStatus.OK);
     }

     /**
     * Returns the specific value with the given ID.
     * @param id Identifier for the value.
     * @return The updated value.
     */
     @GetMapping("/api/values/{id}")
     public ResponseEntity<String> getValue(@PathVariable final String id) {
     return new ResponseEntity<>("value", HttpStatus.OK);
     }

     /**
     * Creates the value.
     * @param value The value.
     * @return The value.
     */
     @PostMapping("api/values")
     public ResponseEntity<String> postValue(@RequestBody final String value) {
     return new ResponseEntity<>(value, HttpStatus.CREATED);
     }

     /**
     * Updates the given value at the ID.
     * @param id Identifier for the value.
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
     * @param id Identifier of the value.
     * @return Returns {@link HttpStatus#NO_CONTENT}
     */
     @DeleteMapping("/api/values/{id}")
     public ResponseEntity<Void> deleteValue(@PathVariable final String id) {
     return new ResponseEntity<>(HttpStatus.NO_CONTENT);
     }

     }
     ```

If you want to try the API locally, run the command `../gradlew bootRun` to run
the example locally.

## Pushing to Pivotal Cloud Foundry (PCF)

Before you can push the application to PCF, create a file in the base directory
called `manifest.yml`:

```yml
applications:
- name: BikeShopAPI
  disk_quota: 512M
  instances: 1
  # Timeout here was increased to three minutes over the default of 1 minute
  # because cranking down the memory settings makes the startup a bit slower,
  # presumably because of GC running and probably also using serial GC.
  timeout: 180
  memory: 256M
  random-route: true
  stack: cflinuxfs3
  path: build/libs/bike-shop-api-spring-0.0.1-SNAPSHOT.jar
  # JAVA_OPTS and buildpack options have been added to tweak down the amount of
  # memory required by the application, and also to specify running the latest
  # version of the JDK (it's okay if you are using older bytecode).
  env:
    JBP_CONFIG_OPEN_JDK_JRE: '{ jre: { version: 11.+ }, memory_calculator: { stack_threads: 20 } }'
    JAVA_OPTS: '-Xss256k -Xms1M -XX:+UseSerialGC -Djava.compiler=none -XX:ReservedCodeCacheSize=40M -XX:MaxDirectMemorySize=1M -Xverify:none -XX:TieredStopAtLevel=1'

```

1. Open a terminal window.

1. Ensure your are targeting the correct space.

   ```bash
   $ cf target
   api endpoint:   <PCF API URL>
   api version:    2.112.0
   user:           <YOUR EMAIL>
   org:            <YOUR ORG NAME>
   space:          <YOUR SPACE NAME>
   ```

1. Build the Spring Boot "uber" jar.

   ```bash
   $ ../gradlew clean bootJar
   ```

1. Push the BikeShop Java API

   ```bash
   $ cf push
   ```

---

## Testing BikeShop.API

1. Get the route for the app.

   ```bash
   $ cf app BikeShopAPI
     Showing health and status for app BikeShopAPI in org <YOUR ORG NAME> / space <YOUR SPACE NAME> as <YOUR EMAIL>...

     name:              BikeShopAPI
     requested state:   started
     routes:            <YOUR ROUTE>
     last uploaded:     Tue 10 Sep 10:56:18 CDT 2019
     stack:             cflinuxfs3
     buildpacks:        client-certificate-mapper=1.11.0_RELEASE container-security-provider=1.16.0_RELEASE
                    java-buildpack=v4.21-offline-https://github.com/cloudfoundry/java-buildpack.git#0bc7378
                    java-main java-opts java-security jvmkill-agent=1.16.0_RELEASE open-jdk...

     type:           web
     instances:      1/1
     memory usage:   256M
          state     since                  cpu    memory          disk             details
     #0   running   2019-09-10T15:57:04Z   0.3%   76.1M of 256M   145.6M of 512M
   ```

1. Test the api in using `http` ([HTTPie](https://httpie.org/)) or `curl`:

     ``` bash
     $ http http://<YOUR ROUTE>/api/values
     HTTP/1.1 200 OK
     Connection: keep-alive
     Content-Length: 19
     Content-Type: application/json;charset=utf-8
     Date: Tue, 10 Sep 2019 16:01:46 GMT
     X-Vcap-Request-Id: faa61508-e33d-4611-6d3e-34ec99c41f32

     [
     "value1",
     "value2"
     ]
     ```

## Recap

So far we have configured our development environment, connected to PCF, created
an app and pushed it to the platform.
