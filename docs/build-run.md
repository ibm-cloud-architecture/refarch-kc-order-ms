# Build and run the order microsercives locally

We support different deployment models: local with docker-compose, and remote using kubernetes on IBM Cloud (IKS), or on Openshift. To build and run we are proposing some scripts. Each script accepts an argument:

* LOCAL (default is argument is omitted)
* IBM_CLOUD

This argument is really used to set environment variables used in the code in a separate script under the `refarch-kc` project and the `scripts` folder.

## Pre-requisites

You can have the following software already installed on your computer or use [our docker images](https://github.com/ibm-cloud-architecture/refarch-kc/blob/master/docker/docker-java-tools) to get those dependencies integrated in docker images, which you can use to build, test and package the java programs.

* [Maven](https://maven.apache.org/install.html)
* Java 8: Any compliant JVM should work.
  * [Java 8 JDK from Oracle](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
  * [Java 8 JDK from IBM (AIX, Linux, z/OS, IBM i)](http://www.ibm.com/developerworks/java/jdk/),
    or [Download a Liberty server package](https://developer.ibm.com/assets/wasdev/#filter/assetTypeFilters=PRODUCT)
    that contains the IBM JDK (Windows, Linux)

## Build

You need to build each microservices independently using maven.

Each microservice has its own build script to perform the maven package and build the docker image. See `scripts` folder under each project.

Any microservice in this repository can be compiled, unit tested and packaged as war file using maven: `mvn package`

* For order-command-ms

 ```
 cd order-command-ms
 ./scripts/buildDocker.sh IBMCLOUD
 ```

* For order-query-ms
 ```
 ./scripts/buildDocker.sh IBMCLOUD
 ```

!!! note
        The build scripta test if the javatool docker image exists and if so they use it, otherwie they use maven.
        If you want to use docker compose use LOCAL as parameter.

* Verify the docker images are created

```
docker images

ibmcase/kc-orderqueryms  latest b85b43980f35 531MB
ibmcase/kc-ordercommandms latest 
```

## Run 

You can always use the maven command to compile and run liberty server for each project. Before doing so be sure to have set the KAFKA_BROKERS and KAFKA_API_KEY environment variables with the command coming from the `refarch-kc` project, which should be at the same level in folder hierarchy as this repository.

```
source ../../refarch-kc/scripts/setenv.sh IBMCLOUD
```

```
mvn install
mvn liberty:run-server
```

But as soon as you need to run integration tests with kafka you need all services up and running.

### With docker compose

To run the complete solution locally we use [docker compose](https://github.com/ibm-cloud-architecture/refarch-kc/blob/master/docker/kc-solution-compose.yml) from the root project.

And to stop everything:

```
docker-compose -f kc-solution-compose.yml down
docker-compose -f backbone-compose.yml down
```
