# Setup

The objective of this lab is to demonstrate a basic project with only a minimal, basic
controller that mirrors the _ValuesController_ in a brand new .NET API project.

When complete, you will have a simple REST API with the endpoint `/api/values` that
you can view both locally and deployed to Pivotal Cloud Foundry (PCF)!

## Logging in to PCF

To use the `cf` command to push your new application to PCF, use the 
`cf login` command as shown here:

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

## Workspace

Set up your work space.

``` bash
$ mkdir -p ~/Workspace
```

Change directories.

``` bash
$ cd ~/Workspace/
```

---

## Create Spring Boot Application

1. Generate a new Spring Boot project called "BikeShop".

     ``` bash
     $ http 'https://start.spring.io/starter.zip?type=gradle-project&language=java&bootVersion=2.1.7.RELEASE&baseDir=bike-shop-api-spring&groupId=com.magenic&artifactId=bike-shop-api-spring&name=bike-shop-api-spring&description=This+is+the+BikeShop+REST+API+implemented+in+SpringpackageName=com.magenic.bikeshopapi&packaging=jar&javaVersion=11&dependenicies=web,actuator' > project.zip
     $ unzip project.zip
     $ cd bike-shop-api-spring
     ```

2. Create a simple web controller with the contents shown here and save to `src/main/java/com/magenic/bikeshopapi/BicycleController.java`:

     ``` java
     package com.magenic.bikeshopapi;

     import org.springframework.web.bind.annotation.GetMapping;
     import org.springframework.web.bind.annotation.RestController;

     @RestController
     public class BicycleController {

          @GetMapping("/bicycles") 
          public String getBicycles() {
               return "Hello, world";
          }

     }
     ```

---

## Pushing to Pivotal Cloud Foundry (PCF)

1. Open a terminal window.

1. Ensure your are targeting the correct space.

   ```bash
   $ cf target
   api endpoint:   https://api.cf.magenic.net
   api version:    2.112.0
   user:           YourUserName@magenic.com
   org:            Training
   space:          YourUserName
   ```

1. Build the Spring Boot "uber" jar.

   ```bash
   $ ./gradlew clean bootJar
   ```

1. Push the BikeShop Java API

   ```bash
   $ cf push
   ```

---

## Testing BikeShop.API

1. Get the route for the app. Where `[YOUR AD NAME]` is your AD user name.

   ```bash
   $ cf app BikeShop-API-[YOUR AD NAME]
   ```

1. The route will be listed next to the app.

   ```bash
   Showing health and status for app BikeShop-API-[YOUR AD NAME] in org Training / space [YOUR AD NAME] as [YOUR AD NAME]@magenic.com...

   name:              BikeShop-API-[YOUR AD NAME]
   requested state:   started
   routes:            bikeshop-api-[YOUR AD NAME].cf.magenic.net
   last uploaded:     Fri 30 Nov 15:12:29 EST 2018
   stack:             cflinuxfs2
   buildpacks:        dotnet-core

   type:           web
   instances:      1/1
   memory usage:   128M
          state     since                  cpu    memory        disk           details
   #0   running   2018-11-30T20:12:37Z   0.1%   25M of 128M   103.1M of 1G
   ```

1. Test the api in using `http` ([HTTPie](https://httpie.org/)) or `curl`:

     ``` bash
     $ http http://bikeshop-api-[YOUR AD NAME].cf.magenic.net/bicycles
     HTTP/1.1 200 OK
     Content-Length: 12
     Content-Type: text/plain;charset=UTF-8
     Date: Mon, 03 Dec 2018 20:58:18 GMT
     X-Vcap-Request-Id: c95add65-31a3-4201-5e20-7e7323ba3ce0

     Hello, world
     ```

## Recap

So far we have configured our development environment, connected to PCF, created
an app and pushed it to the platform.
