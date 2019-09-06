# Connectors

## Continuing from Setup

This lab continues from the SetUp lab and assumes that you have all of the work in that lab completed.

### Open BikeShop

1. Open the solution folder `~/Workspace/cloud-native-lab` in your favorite
Integrated Development Environment (IDE).

2. Open a terminal and change directories.

    ```bash
    cd ~/Workspace/cloud-native-lab
    ```

---

### Adding New Dependencies

There are some dependencies that, when added to Spring, will assist in automatically configuring the data repositories for Spring in Pivotal Cloud Foundry (PCF).

Add the following dependencies to your `build.gradle` file.

```gradle
dependencies {
	compileOnly('org.projectlombok:lombok')
	implementation 'org.apache.httpcomponents:httpclient:4.5.6'
	implementation('org.springframework.boot:spring-boot-starter-cloud-connectors')
	implementation('org.springframework.boot:spring-boot-starter-data-jpa')
	implementation('org.springframework.boot:spring-boot-starter-web')
	runtimeOnly('mysql:mysql-connector-java')
	runtimeOnly('com.h2database:h2')
	testImplementation('org.springframework.boot:spring-boot-starter-test')
}
```



---

## Adding a New Model

The `ValuesController` in the _SetUp_ lab simply returned a list of hard-coded values, but for this lab you will see how a model using JPA (Java Persistence API) code can be used to define a structure both in the database and in HTTP request and response messages. 

> Note: in many production environments, there may be different classes to represent messages and data structures. For this lab, it is okay to keep things simple for clarity.

Add the **Bicycle** model object by following these steps below.

1. Create a `models` directory.

   ```bash
   mkdir -p ./src/main/java/com/magenic/cloudnativelab/models
   ```

1. Create a `Bicycle.java` file in the `src/main/java/com/magenic/cloudnativelab/models`
directory.

1. Add the following contents to `Bicycle.java` file.

    ```java
    package com.magenic.cloudnativelab.models;

    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import javax.persistence.Entity;
    import javax.persistence.GeneratedValue;
    import javax.persistence.Id;

    @Data
    @Entity
    @AllArgsConstructor
    @NoArgsConstructor
    public class Bicycle {
       @Id
       @GeneratedValue
       private long id;
       private String productName;
       private double price;
       private String description;
       private String image;
    }  
    ```

---

## Adding a Repository

Now that you have a **Bicycle** model object, you need a repository that will be used to get the object from the database and map the data from the database tables to Java objects. 

In Spring, this is mostly done for you when you create an interface that extends the `CrudRepository` interface. Extending this interface handles the basic create, read, update, and delete (CRUD) operations for you.

1. Create the `repositories` directory.

    ```bash
    mkdir -p ./src/main/java/com/magenic/cloudnativelab/repositories
    ```

2. Create a `BicycleRepository.java` file in the `src/main/java/com/magenic/cloudnativelab/repositories` directory.

3. Add the following contents to the `BicycleRepository.java` file.

    ```java
    package com.magenic.cloudnativelab.repositories;

    import com.magenic.cloudnativelab.models.Bicycle;
    import org.springframework.data.repository.CrudRepository;

    /**
     * Spring data repository for handling {@link Bicycle} objects.
     */
    public interface BicycleRepository extends CrudRepository<Bicycle, Long> {
    }
    ```

---

## Adding a Service

Once you have a repository that will handle the database CRUD operations, you can add a service that will talk to the repository. 

In this lab, the service is mostly a pass-through that calls methods from the repository and returns the values as-is. 
However, in most production scenerios the service adds additional value, such a performing additional validation, formatting, and more. Even though it might be tempting here to remove the service completely and use the repository directly from the controller, it is generally good practice to use a service pattern as it adds another layer of abstraction that insulates the controller from repository changes. 

1. Create the `services` directory.

    ```bash
    mkdir -p ./src/main/java/com/magenic/cloudnativelab/services
    ```

2. Create a `BicycleService.java` file in the `src/main/java/com/magenic/cloudnativelab/services` directory.

3. Add the following to the `BicycleService.java` file.

    ```java
    package com.magenic.cloudnativelab.services;

    import java.util.Optional;

    import com.magenic.cloudnativelab.models.Bicycle;
    import com.magenic.cloudnativelab.repositories.BicycleRepository;

    import org.springframework.stereotype.Component;

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
    }    
    ```

---

## Adding a Controller

In Spring, a class marked with the `RestController` attribute can be used to define methods that handle
the HTTP requests. In this step, you will add a controller that handles basic GET and POST requests using the `GetMapping` and `PostMapping`, respectively.

1. Create a `BicycleController.java` file in the `src/main/java/com/magenic/cloudnativelab/controllers` directory.

1. Add the following to the `BicycleController.java` file.

    ```java
    package com.magenic.cloudnativelab.controllers;

    import com.magenic.cloudnativelab.models.Bicycle;
    import com.magenic.cloudnativelab.models.ItemNotFoundException;
    import com.magenic.cloudnativelab.services.BicycleService;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RestController;

    /**
     * REST Controller for the {@link Bicycle}.
     */
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
            return this.service.getBicycle(id).orElseThrow(() -> new ItemNotFoundException(id));
        }

        /**
         * Creates a new {@link Bicycle} object in the database.
         * @param bicycle
         * @return
         */
        @PostMapping("/api/bicycle")
        public Bicycle post(@RequestBody Bicycle bicycle) {
            return this.service.createBicycle(bicycle);
        }

    }
    ```

This new controller will only service three enddpoints for now. Two of them are endpoints that handle HTTP GET requests: `/api/bicycle`, which returns all of the objects from the data store and `/api/bicycle/{id}`, which returns a single object.

One HTTP POST endpoint allows you to add new objects to the data store.

---

## Adding a Database Initializer

A database initializer will pre-populate the data repository with some values so there is something
interesting to see when the service is first started. 

1. Create a `BicycleDbInitialize.java` file in the `src/main/java/com/magenic/cloudnativelab` directory.

2. Add the following to the `BicycleDbInitialize.java` file.

    ```java
    package com.magenic.cloudnativelab;

    import com.magenic.cloudnativelab.models.Bicycle;
    import com.magenic.cloudnativelab.repositories.BicycleRepository;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;

    @Configuration
    @Slf4j
    class BicycleDbInitialize {

        /**
        * Initializes the database with a few objects.
        * @param repository
        * @return
        */
        @Bean
        CommandLineRunner initDatabase(BicycleRepository repository) {
            return args -> {
                log.info("Preloading " + repository.save(new Bicycle(1, "Schwinn Mountain Bike", 899.99, "Schwinn", "./assets/images/bike5.jpg")));
                log.info("Preloading " + repository.save(new Bicycle(2, "Nishiki Dirt Bike", 399.99, "Nishiki", "./assets/images/bike6.jpg")));
            };
        }
    }
    ```

When the Spring project starts up for the first time, this code will run and will use the repository (`BicycleRepository`) to add the objects.

---

## Updating the Configuration

This project uses [Java config in Spring](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/Configuration.html) to configure the data source (implementation of the `DataSource` interface) 
for the `cloud` profile. 

1. Create a `CloudDataSourceConfig.java` file in the `src/main/java/com/magenic/cloudnativelab` directory.

1. Add the following contents to the `CloudDataSourceConfig.java` file.

    ```java
    package com.magenic.cloudnativelab;

    import org.springframework.cloud.config.java.AbstractCloudConfig;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.context.annotation.Profile;

    import javax.sql.DataSource;

    @Configuration
    @Profile("cloud")
    public class CloudDataSourceConfig extends AbstractCloudConfig {

        @Bean
        public DataSource dataSource() {
            return connectionFactory().dataSource();
        }
    }
    ```

After adding the Java configuration, there are a few changes to make for the `application.yml` file that
set some configuration values specific to `mysql` profile. Edit the `src/main/resources/application.yml`
to look like this:

```yaml
# TODO: If you are running into any issues during startup, uncomment
# the following line.
# debug: true
logging:
  pattern:
    console: "%d [%t] %-5p - %m%n"
spring:
  main:
    banner-mode: "off"

---
spring:
  profiles: "local"
  datasource:
    url: "jdbc:h2:mem:testdb"
    username: "sa"
    password:
    driver-class-name: "org.h2.Driver"
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect

---
spring:
  profiles: "mysql"
  jpa:
    show-sql: true
    database-platform: org.hibernate.dialect.MySQL5Dialect
    hibernate:
      ddl-auto: update

```



## Creating the Database 

1. Make sure you are logged in the PCF foundation:

    ```bash
    cf target
    ```

2. You will see the following output:

    ```bash
    api endpoint:   https://api.cf.magenic.net
    api version:    2.112.0
    user:           [YOUR USER NAME]@magenic.com
    org:            sandbox
    space:          [YOUR USER NAME]
    ```

3. Check and see what services are available:

    ```bash
    cf marketplace
    ```

 4. Should give you the following results:

    ```bash
    service                       plans                       description
    app-autoscaler                standard                    Scales bound applications in response to load
    p-circuit-breaker-dashboard   standard                    Circuit Breaker Dashboard for Spring Cloud Applications
    p-config-server               standard                    Config Server for Spring Cloud Applications
    p-rabbitmq                    standard                    RabbitMQ service to provide shared instances of this high-performance multi-protocol messaging broker.
    p-redis                       dedicated-vm, shared-vm     Redis service to provide pre-provisioned instances configured as a datastore, running on a shared or dedicated VM.
    p-service-registry            standard                    Service Registry for Spring Cloud Applications
    p.mysql                       db-micro                    Dedicated instances of MySQL
    p.rabbitmq                    single-node-3.7             RabbitMQ service to provide dedicated instances of this high-performance multi-protocol messaging broker
    p.redis                       cache-small, cache-medium   Redis service to provide on-demand dedicated instances configured as a cache.
    ```

 5. Create a MySQL instance: (note this take a few mins to complete)

    ```bash
    cf create-service p.mysql db-micro BikeShopDB
    ```

 If you are using the PCF services online, use the following command:

    ```bash
    cf create-service cleardb spark mysql
    ``` 

 6. Bind BikeShop App to the BikeShopDB (note [YOUR USER NAME] is your user name)

    ```bash
    cf bind-service BikeShop-API-[YOUR USER NAME] BikeShopDB
    ```


--- 

## Upating the PCF Manifest

There are some updates to the `manifest.yml` to set the Spring cloud profile using the `SPRING_PROFILES_ACTIVE` environment variable and to map the newly-created database service (_mysql_, in this example) to the application.

Edit the `manifest.yml` file so that it looks like the example  here:

```yaml
applications:
- name: bikeshopapi
  disk_quota: 1G
  instances: 1
  memory: 728M
  random-route: true
  stack: cflinuxfs2
  path: build/libs/cloud-native-lab-0.0.1-SNAPSHOT.jar
  env:
    SPRING_PROFILES_ACTIVE: cloud,mysql
  services:
    - mysql
```

> Note: If you used _BikeShopDB_ for your database name, use that instead of _mysql_ under the `services` tag.

---

## Pushing the BikeShop API to PCF

1. Build a Spring Boot Java ARchive (JAR) which will include the compiled files and all of the dependencies:

    ```bash
    ./gradlew bootJar
    ```

2. Push BikeShop.API

    ```bash
    cf push bikeshopapi
    ```

---

## Testing the BikeShop API

Since the database should have been prepopulated with a few objects with the database initializer code,
you should be able to test the API after pushing it with either the browser or a command line tool, 
such as `http` ([HTTPie](https://httpie.org/)).

To test the service, you will need the base URL of your application. Find out the base URL by using
the command `cf apps` and looking under urls for the _bicycleapi_ app.

Once you have the base URL, test the `api/bicycle` API by navigating to in with your browser 
(`https://bikeshop-api-[YOUR AD NAME].cf.magenic.net/api/bicycle/1`) or by typing the command:

```sh
http bicycleapi-shiny-numbat.cfapps.io/api/bicycle
```

```json
HTTP/1.1 200 OK
Connection: keep-alive
Content-Length: 241
Content-Type: application/json;charset=UTF-8
Date: Mon, 21 Jan 2019 17:10:50 GMT
X-Vcap-Request-Id: c0d9fb70-f7db-40ed-7f2e-35c41cd35f76

[
    {
        "description": "Schwinn",
        "id": 1,
        "image": "./assets/images/bike5.jpg",
        "price": 899.99,
        "productName": "Schwinn Mountain Bike"
    },
    {
        "description": "Nishiki",
        "id": 2,
        "image": "./assets/images/bike6.jpg",
        "price": 399.99,
        "productName": "Nishiki Dirt Bike"
    }
]
```

---

## Summary

So far you have continued from the previous lab by creating a database service and binding it to the application. 
You added code to configure the database connection and to create sample entries in the database. Finally, 
you tested the API using a browser or CLI tool to view the JSON output.

You can now proceed onto the next lab, _Configuration_.
