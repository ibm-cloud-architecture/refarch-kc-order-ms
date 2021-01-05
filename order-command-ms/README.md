# Shipment Order Command Microservice

The order command microservice supports the following features:

* Implement the Command part of the CQRS pattern
* Expose a POST `/orders` end point to get an Shipment Order
* Emit an OrderCreateCommand event to Kafka to use the topic as a 'transaction log', as soon as the POST request is received.
* Consume Order*Command events to act on the data store. The data store is an in memory hash map. 
* Emit OrderCreated, OrderUpdated, OrderCancelled events to the `order` kafka topic for other to consume. One of those service will be the Query part of the CQRS service.

This Java microservice applications runs on [WebSphere Liberty](https://developer.ibm.com/wasdev/).

See the documentation in [this chapter](hthttps://ibm-cloud-architecture.github.io/refarch-kc/microservices/order-command/) for more explanations.


## Configuration

The application is configured to provide JAX-RS REST capabilities, JSON parsing and Contexts and Dependency Injection (CDI).

These capabilities are provided through dependencies in the pom.xml file and Liberty features enabled in the server config file found in `src/main/liberty/config/server.xml`.

## Endpoints

The application exposes the following endpoints:

* Health endpoint: `<host>:<port>/<contextRoot>/health`
* `<host>:<port>/<contextRoot>/orders` GET, POST
* `<host>:<port>/<contextRoot>/orders/{id}` GET, PUT
* `<host>:<port>/<contextRoot>/orders/cancel/{id}` POST 

The context root is set in the `src/main/webapp/WEB-INF/ibm-web-ext.xml` file. The port is set in the pom.xml file.

## Notices

This project was generated using IBM Cloud Microservice Starter for Java - MicroProfile / Java EE
[![](https://img.shields.io/badge/IBM%20Cloud-powered-blue.svg)](https://ibmcloud.com)
[![Platform](https://img.shields.io/badge/platform-java-lightgrey.svg?style=flat)](https://www.ibm.com/developerworks/learn/java/)

