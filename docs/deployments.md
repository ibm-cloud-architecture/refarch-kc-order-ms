# Deployments

Be sure to have read the [build and run article before](./build-run.md).

## Deploy to IKS

Be sure to have [read and done the steps](https://ibm-cloud-architecture.github.io/refarch-kc/analysis/readme/) to prepare the IBM Cloud services and get a kubernetes cluster up and running.


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


## Deploy to ICP

N/A

## Deploy to OpenShift Container Platform (OCP)

### Deploy to OCP 3.11

**Cross-component deployment prerequisites:** _(needs to be done once per unique deployment of the entire application)_
1. If desired, create a non-default Service Account for usage of deploying and running the K Container reference implementation.  This will become more important in future iterations, so it's best to start small:
  - Command: `oc create serviceaccount -n <target-namespace> kcontainer-runtime`
  - Example: `oc create serviceaccount -n eda-refarch kcontainer-runtime`
2. The target Service Account needs to be allowed to run containers as `anyuid` for the time being:
  - Command: `oc adm policy add-scc-to-user anyuid -z <service-account-name> -n <target-namespace>`
  - Example: `oc adm policy add-scc-to-user anyuid -z kcontainer-runtime -n eda-refarch`
  - NOTE: This requires `cluster-admin` level privileges.

2. Create kafka-brokers ConfigMap
  - Command: `kubectl create configmap kafka-brokers --from-literal=brokers='<replace with comma-separated list of brokers>' -n <namespace>`
  - Example: `kubectl create configmap kafka-brokers --from-literal=brokers='broker-3-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-2-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-1-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-5-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-0-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-4-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093'`
3. Create optional eventstreams-apikey Secret
  - Command: `kubectl create secret generic eventstreams-apikey --from-literal=binding='<replace with api key>' -n <namespace>`
  - Example: `kubectl create secret generic eventstreams-apikey --from-literal=binding='z...12345...notanactualkey...67890...a'`

**Perform the following for both `order-command-ms` and `order-query-ms` microservices:**
1. Build and push Docker images
  1. Create a Jenkins project, pointing to the remote GitHub repository for the Order Microservices, creating the necessary parameters:
    - Create a String parameter named `REGISTRY` to determine a remote registry that is accessible from your cluster
    - Create a String parameter named `REGISTRY_NAMESPACE` to describe the registry namespace to push image into
    - Create a String parameter named `IMAGE_NAME` which should be self-expalantory
    - Create a String parameter named `CONTEXT_DIR` to determine the correct working directory to work from inside the source code, with respect to the root of the repository
    - Create a String parameter named `DOCKERFILE` to determine the desired Dockerfile to use to build the Docker image.  This is determined with respect to the `CONTEXT_DIR` parameter.
    - Create a Credentials parameter named `REGISTRY_CREDENTIALS` and assign the necessary credentials to allow Jenkins to push the image to the remote repository
  2. Manually build the Docker image and push it to a registry that is accessible from your cluster (Docker Hub, IBM Cloud Container Registry, manually deployed Quay instance):
    - `docker build -t <private-registry>/<image-namespace>/order-command-ms:latest order-command-ms/`
    - `docker build -t <private-registry>/<image-namespace>/order-query-ms:latest order-query-ms/`
    - `docker login <private-registry>`
    - `docker push <private-registry>/<image-namespace>/order-command-ms:latest`
    - `docker push <private-registry>/<image-namespace>/order-query-ms:latest`
3. Generate application YAMLs via `helm template`:
  - Parameters:
    - `--set image.repository=<private-registry>/<image-namespace>/<image-repository>`
    - `--set image.tag=latest`
    - `--set image.pullSecret=<private-registry-pullsecret>` (optional or set to blank)
    - `--set image.pullPolicy=Always`
    - `--set eventstreams.env=ICP`
    - `--set serviceAccountName=<service-account-name>`
    - `--namespace <target-namespace>`
    - `--output-dir <local-template-directory>`
  - Example: `helm template --set image.repository=rhos-quay.internal-network.local/browncompute/order-command-ms --set image.tag=latest --set image.pullSecret= --set image.pullPolicy=Always --set eventstreams.env=ICP --set serviceAccountName=kcontainer-runtime --output-dir templ --namespace eda-refarch chart/ordercommandms/`
4. Deploy application using `oc apply`:
  - `oc apply -f templates/ordercommandms/templates`
