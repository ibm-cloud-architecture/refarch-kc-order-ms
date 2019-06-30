# Build and run the order microsercives locally

We support different deployment models: local with docker-compose, local with minikube, and remote using kubernetes on IBM Cloud (IKS), or on IBM Cloud private. To build and run we are proposing some scripts. Each script accepts an argument:

* LOCAL (default is argument is omitted)
* MINIKUBE
* IBM_CLOUD
* ICP

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

Each microservice has its ownbuild script to perform the maven package and build the docker image. See `scripts` folder under each project.

* For order-command-ms

 ```
 cd order-command-ms
 ./scripts/buildDocker.sh MINIKUBE
 ```

* For order-query-ms
 ```
 ./scripts/buildDocker.sh MINIKUBE
 ```

!!! note
        The build scripts test if the javatool docker image exists and they use it, if found, otherwie they use maven.
        If you want to use docker compose use LOCAL as parameter.

* Verify the docker images are created

```
docker images

ibmcase/kc-orderqueryms  latest b85b43980f35 531MB
ibmcase/kc-ordercommandms latest 
```

## Run 

You can always use the maven command to compile and run liberty server for each project.
```
mvn install
mvn liberty:run-server
```

But as soon as you need to run integration tests with kafka you need all services up and running.

### On Minikube

For the order command microservice:

```
cd order-command-ms

helm install chart/ordercommandms/ --name ordercmd --set image.repository=ibmcase/kc-ordercommandms --set image.pullSecret= --set image.pullPolicy=Never --set eventstreams.brokers=kafkabitmani:9092 --set eventstreams.env=MINIKUBE --namespace greencompute
```

or use the command: `./scripts/deployHelm MINIKUBE`

Without any previously tests done, the call below should return an empty array: `[]`
```
curl http://localhost:31200/orders
```

We will present some integration tests in a section below.

### With docker compose

To run the complete solution locally we use [docker compose](https://github.com/ibm-cloud-architecture/refarch-kc/blob/master/docker/kc-solution-compose.yml) from the root project.

And to stop everything:

```
docker-compose -f kc-solution-compose.yml down
docker-compose -f backbone-compose.yml down
```
