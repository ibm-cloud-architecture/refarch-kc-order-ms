# Deployments

Be sure to have read the [build and run article before](./build-run.md).

## Deploy locally on Minikube


## Deploy locally using docker-compose

The approach is simple, compile, build the war file, build the docker image and run docker compose for the backend solution.

To start the solution go to the `refarch-kc/docker` folder

```
source ../scripts/setenv.sh LOCAL
docker-compose -f kc-solution-compose.yml up
```

## Deploy to IKS


## Deploy to ICP