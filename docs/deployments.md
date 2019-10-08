# Deployments

Be sure to have read the [build and run article before](./build-run.md).

## Deployment prerequisites

Regardless of specific deployment targets (OCP, IKS, k8s), the following prerequisite Kubernetes artifacts need to be created to support the deployments of application components.  These artifacts need to be created once per unique deployment of the entire application and can be shared between application components in the same overall application deployment.

1. Create `kafka-brokers` ConfigMap

    - Command: `kubectl create configmap kafka-brokers --from-literal=brokers='<replace with comma-separated list of brokers>' -n <namespace>`
    - Example: `kubectl create configmap kafka-brokers --from-literal=brokers='broker-3-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-2-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-1-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-5-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-0-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-4-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093' -n eda-refarch`

2. Create optional `eventstreams-apikey` Secret, if you are using Event Streams as your Kafka broker provider. Get the API key from the user interface using an administrator user.
    - Command: `kubectl create secret generic eventstreams-apikey --from-literal=binding='<replace with api key>' -n <namespace>`
    - Example: `kubectl create secret generic eventstreams-apikey --from-literal=binding='z...12345...notanactualkey...67890...a' -n eda-refarch`

3. If you are using Event Streams as your Kafka broker provider and it is deployed via the IBM Cloud Pak for Integration (ICP4I), you will need to create an additional Secret to store the generated Certificates & Truststores.
    - From the "Connect to this cluster" tab on the landing page of your Event Streams installation, download both the **Java truststore** and the **PEM certificate**.
    - Create the Java truststore Secret:
        - Command: `kubectl create secret generic <secret-name> --from-file=/path/to/downloaded/file.jks`
        - Example: `kubectl create secret generic es-truststore-jks --from-file=/Users/osowski/Downloads/es-cert.jks`
    - Create the PEM certificate Secret:
        - Command: `kubectl create secret generic <secret-name> --from-file=/path/to/downloaded/file.pem`
        - Example: `kubectl create secret generic es-ca-pemfile --from-file=/Users/osowski/Downloads/es-cert.pem`

!!! note
      The name of those secrets are used in the Helm chart `values.yaml` and `deployment.yaml` files of each project.

      ```
      kafka:
        brokersConfigMap: kafka-brokers
      eventstreams:
        enabled: true
        apikeyConfigMap: eventstreams-apikey
        truststoreRequired: true
        truststorePath: /config/resources/security/es-ssl
        truststoreFile: es-cert.jks
        truststoreSecret: es-truststore-jks
        truststorePassword: changeit
      ```

## Deploy to IKS

Be sure to have created an IBM kubernetes service cluster (See [this lab](https://ibm-cloud-architecture.github.io/refarch-eda/training/eda-skill-journey/#5-hands-on-lab-prepare-ibm-cloud-iks-openshift-environment) for detail)


Once the two docker images are built, upload them to the IKS private registry

```
docker push us.icr.io/ibmcaseeda/kc-orderqueryms
docker push us.icr.io/ibmcaseeda/kc-ordercmdms
```

* Verify the images are in you private repo:

```shell
ibmcloud cr image-list
```

* Deploy the helm charts for each service using the `scripts/deployHelm` under each project.

```
cd order-command-ms
./scripts/deployHelm MINIKUBE

cd order-query-ms
./scripts/deployHelm MINIKUBE
```

* Verify the deployments and pods:

```shell
kubectl get deployments -n browncompute
```
>  | NAME | DESIRED | CURRENT  | UP-TO-DATE  | AVAILABLE  | AGE |
  | --- | --- | --- | --- | --- | --- |
  | fleetms-deployment  |  1  |       1     |    1     |       1     |      23h |
  | kc-ui              |  1  |  1  |  1   |  1  |     18h |
  | ordercommandms-deployment | 1  | 1  | 1  |  1  |   1d |
  | orderqueryms-deployment | 1  |   1 |  1  |  1  |   23h  |
  | voyagesms-deployment |   1   |  1  |  1  |  1  |   19h |


## Deploy to OpenShift Container Platform (OCP)

### Deploy to OCP 3.11

**Cross-component deployment prerequisites:** _(needs to be done once per unique deployment of the entire application)_

1. If desired, create a non-default Service Account for usage of deploying and running the Reefer Container reference implementation.  This will become more important in future iterations, so it's best to start small:
    - Command: `oc create serviceaccount -n <target-namespace> kcontainer-runtime`
    - Example: `oc create serviceaccount -n eda-refarch kcontainer-runtime`

2. The target Service Account needs to be allowed to run containers as `anyuid` for the time being:
    - Command: `oc adm policy add-scc-to-user anyuid -z <service-account-name> -n <target-namespace>`
    - Example: `oc adm policy add-scc-to-user anyuid -z kcontainer-runtime -n eda-refarch`
    - NOTE: This requires `cluster-admin` level privileges.

**Perform the following for both `order-command-ms` and `order-query-ms` microservices:**

1. Build and push the Docker image by one of the two options below:
    - Create a Jenkins project, pointing to the remote GitHub repository for the `order-command` and `order-query` microservices, and manually creating the necessary parameters.  
    
      Refer to the `order-command` [`Jenkinsfile.NoKubernetesPlugin`](https://github.com/ibm-cloud-architecture/refarch-kc-order-ms/blob/master/order-command-ms/Jenkinsfile.NoKubernetesPlugin) or `order-query` [`Jenkinsfile.NoKubernetesPlugin`](https://github.com/ibm-cloud-architecture/refarch-kc-order-ms/blob/master/order-query-ms/Jenkinsfile.NoKubernetesPlugin) for appropriate parameter values.

    - Manually build the Docker image and push it to a registry that is accessible from your cluster (Docker Hub, IBM Cloud Container Registry, manually deployed Quay instance):
      
        - `docker build -t <private-registry>/<image-namespace>/order-command-ms:latest order-command-ms/`
        - `docker build -t <private-registry>/<image-namespace>/order-query-ms:latest order-query-ms/`
        - `docker login <private-registry>`
        - `docker push <private-registry>/<image-namespace>/order-command-ms:latest`
        - `docker push <private-registry>/<image-namespace>/order-query-ms:latest`

2. Generate application YAMLs via `helm template` for both `order-command` and `order-query`:
    - Parameters:
        - `--set image.repository=<private-registry>/<image-namespace>/<image-repository>`
        - `--set image.pullSecret=<private-registry-pullsecret>` (optional or set to blank)
        - `--set kafka.brokersConfigMap=<kafka brokers ConfigMap name>`
        - `--set eventstreams.enabled=(true/false)` (`true` when connecting to Event Streams of any kind, `false` when connecting to Kafka directly)
        - `--set eventstreams.apikeyConfigMap=<kafka api key Secret name>`
        - `--set eventstreams.truststoreRequired=(true/false)` (`true` when connecting to Event Streams via ICP4I)
        - `--set eventstreams.truststoreSecret=<eventstreams jks file secret name>` (only used when connecting to Event Streams via ICP4I)
        - `--set eventstreams.truststorePassword=<eventstreams jks password>` (only used when connecting to Event Streams via ICP4I)
        - `--set serviceAccountName=<service-account-name>`
        - `--namespace <target-namespace>`
        - `--output-dir <local-template-directory>`
    - Example using Event Streams via ICP4I:
   
      ```shell
      helm template --set image.repository=rhos-quay.internal-network.local/browncompute/order-command-ms --set image.pullSecret= --set kafka.brokersConfigMap=es-kafka-brokers --set eventstreams.enabled=true --set eventstreams.apikeyConfigMap=es-eventstreams-apikey --set serviceAccountName=kcontainer-runtime --set eventstreams.truststoreRequired=true --set eventstreams.truststoreSecret=es-truststore-jks --set eventstreams.truststorePassword=password --output-dir templates --namespace eda-pipelines-sandbox chart/ordercommandms
      ```
  - Example using Event Streams hosted on IBM Cloud:
      ```shell
      helm template --set image.repository=rhos-quay.internal-network.local/browncompute/order-command-ms --set image.pullSecret= --set kafka.brokersConfigMap=kafka-brokers --set eventstreams.enabled=true --set eventstreams.apikeyConfigMap=eventstreams-apikey --set serviceAccountName=kcontainer-runtime --output-dir templates --namespace eda-pipelines-sandbox chart/ordercommandms
      ```

4. Deploy application using `oc apply`:
      - `oc apply -f templates/ordercommandms/templates`
