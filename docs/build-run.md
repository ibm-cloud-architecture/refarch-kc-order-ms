# Build and run the order microsercives locally

We support different deployment models: local with docker-compose, and remote using kubernetes on IBM Cloud (IKS), or on premise with Openshift. To build and run we are proposing to use some shell scripts. Each script accepts one argument:

* LOCAL (default is argument is omitted)
* IBM_CLOUD

This argument is used to set environment variables used in the code.  In fact the `setenv.sh` script is defined in the root `refarch-kc` project and the `scripts` folder.

## Pre-requisites

You can have the following software already installed on your computer or use [our docker images](https://github.com/ibm-cloud-architecture/refarch-kc/blob/master/docker/docker-java-tools) to get those dependencies integrated in docker images, which you can use to build, test and package the java programs.

* [Maven](https://maven.apache.org/install.html)
* Java 8: Any compliant JVM should work like:
  * [Java 8 JDK from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * [Java 8 JDK from IBM (AIX, Linux, z/OS, IBM i)](http://www.ibm.com/developerworks/java/jdk/)
* For IBMCLOUD, you need to be sure to have an Event Stream service defined (See [this note](https://ibm-cloud-architecture.github.io/refarch-kc/deployments/backing-services/#using-ibm-event-streams-hosted-on-ibm-cloud) for a simple summary of what to do). 
* If you run IBM Event Streams on openshift cluster on premise servers, be sure to get truststore certificates and API key.
* Configure the following topics for both microservices: `orderCommands`, `errors`, `orders`. You can use the script `createTopics.sh` in the `refarch-kc` project for that or use the Event Streams user interface.
   

## Build

There are two separate folder to manage the code and scripts for the CQRS command and query part:

* order-command-ms
* order-query-ms

You need to build each microservice independently using maven.

Each microservice has its own build script to perform the maven package and build the docker image. See `scripts` folder under each project.

Any microservice in this repository can be compiled, unit tested and packaged as war file using maven: `mvn package`

* For order-command-ms, the following command will run unit tests and package the war file, then build a docker image

 ```
 cd order-command-ms
 ./scripts/buildDocker.sh IBMCLOUD
 ```

If you want to run the integration test you need to do the following:
```
source ../../refarch-kc/scripts/setenv.sh IBMCLOUD
mvn install   
or
mvn integration-test
```

* For order-query-ms

 ```
 ./scripts/buildDocker.sh IBMCLOUD
 ```

!!! note
        The build scripts test if the javatool docker image exists and if so they use it, otherwise they use maven.
        If you want to use docker compose use LOCAL as parameter.

* Verify the docker images are created

```
docker images

ibmcase/kc-orderqueryms  latest b85b43980f35 531MB
ibmcase/kc-ordercommandms latest 
```

## Run 

You can always use the maven command to compile and run liberty server for each project. Before doing so be sure to have set the KAFKA_BROKERS and KAFKA_API_KEY environment variables with the `setenv.sh` command coming from the `refarch-kc` project, which should be at the same level in folder hierarchy as this repository.

```
source ../../refarch-kc/scripts/setenv.sh IBMCLOUD
```

```
mvn install
mvn liberty:run-server
```

### With docker compose

To run the complete solution locally we use [docker compose](https://github.com/ibm-cloud-architecture/refarch-kc/blob/master/docker/kc-solution-compose.yml) from the root `refarch-kc` project.

```
docker-compose -f backbone-compose.yml up
docker-compose -f kc-solution-compose.yml up
```

And to stop everything:

```
docker-compose -f kc-solution-compose.yml down
docker-compose -f backbone-compose.yml down
```

## Deploy on kubernetes cluster

See [this note.](deployments.md)
