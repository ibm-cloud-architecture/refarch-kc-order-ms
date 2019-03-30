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



This Java microservice applications runs on [WebSphere Liberty](https://developer.ibm.com/wasdev/).




### Configuration
The application is configured to provide JAX-RS REST capabilities, JNDI, JSON parsing and Contexts and Dependency Injection (CDI).

These capabilities are provided through dependencies in the pom.xml file and Liberty features enabled in the server config file found in `src/main/liberty/config/server.xml`.

### Run




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
