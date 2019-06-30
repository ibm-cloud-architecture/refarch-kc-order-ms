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