## Order Command Microservice

This project is part of the EDA reference implementation solution. 

### Table of Contents
* [Summary](#summary)
* [Requirements](#requirements)
* [Configuration](#configuration)
* [Project contents](#project-contents)
* [Run](#run)

### Summary

The order command microservice supports the following features:

* Create a new order via POST to `/orders`: emit a OrderCreated event and save to internal data store. (memory only)
* Update existing order via PUT on `/orders/:id` 
See the class [OrderCRUDService.java](https://github.com/ibm-cloud-architecture/refarch-kc-order-ms/blob/master/order-command-ms/src/main/java/ibm/labs/kc/order/command/service/OrderCRUDService.java).
* Produce order events to the `orders` topic. 
* Consume events to update the state of the order or enrich it with new elements.

When the application starts there is a [ServletContextListener](https://docs.oracle.com/javaee/6/api/javax/servlet/ServletContextListener.html) class started to create a consumer to subscribe to order events (different types) from `orders` topic. When consumer reaches an issue to get event it creates an error to the `errors` topic, so administrator user could replay the event source from the last committed offset. Any kafka broker communication issue is shutting down the consumer loop.


This Java microservice applications runs on [WebSphere Liberty](https://developer.ibm.com/wasdev/).


### Requirements
* [Maven](https://maven.apache.org/install.html)
* Java 8: Any compliant JVM should work.
  * [Java 8 JDK from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * [Java 8 JDK from IBM (AIX, Linux, z/OS, IBM i)](http://www.ibm.com/developerworks/java/jdk/),
    or [Download a Liberty server package](https://developer.ibm.com/assets/wasdev/#filter/assetTypeFilters=PRODUCT)
    that contains the IBM JDK (Windows, Linux)

### Configuration
The application is configured to provide JAX-RS REST capabilities, JNDI, JSON parsing and Contexts and Dependency Injection (CDI).

These capabilities are provided through dependencies in the pom.xml file and Liberty features enabled in the server config file found in `src/main/liberty/config/server.xml`.

### Run

To build and run the application:
1. `mvn install` or `mvn install  -DskipITs` to bypass integration tests, which need kafka and a running Liberty server
1. `mvn liberty:run-server`  to start the server with the deployed wars.


To run the application in Docker use the Docker file called `Dockerfile`. If you do not want to install Maven locally you can use `Dockerfile-tools` to build a container with Maven installed.

### Endpoints

The application exposes the following endpoints:
* Health endpoint: `<host>:<port>/<contextRoot>/health`

The context root is set in the `src/main/webapp/WEB-INF/ibm-web-ext.xml` file. The ports are set in the pom.xml file and exposed to the CLI in the cli-config.yml file.

### Notices

This project was generated using IBM Cloud Microservice Starter for Java - MicroProfile / Java EE
[![](https://img.shields.io/badge/IBM%20Cloud-powered-blue.svg)](https://ibmcloud.com)
[![Platform](https://img.shields.io/badge/platform-java-lightgrey.svg?style=flat)](https://www.ibm.com/developerworks/learn/java/)

* generator-ibm-java v5.13.7
* generator-ibm-service-enablement v3.1.2
* generator-ibm-cloud-enablement v1.5.4
* generator-ibm-java-liberty 
