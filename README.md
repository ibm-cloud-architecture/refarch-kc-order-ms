# IBM Garage Event-Driven Reference Architecture

## Reefer Container Shipment Order Management

This project is a component of the [Reefer Container Shipment reference implementation](https://ibm-cloud-architecture.github.io/refarch-kc/) of the [IBM Garage Event-Driven Architecture](https://ibm-cloud-architecture.github.io/refarch-eda/) reference architecture. It implements [CQRS](https://ibm-cloud-architecture.github.io/refarch-eda/patterns/cqrs/) and [event sourcing](https://ibm-cloud-architecture.github.io/refarch-eda/patterns/event-sourcing/) patterns to support the order management process of shipping fresh cargo from the manufacturer to its target destination.

For complete documentation on this project and its peer microservices, reference the **[Order Command](https://ibm-cloud-architecture.github.io/refarch-kc/microservices/order-command/)** and **[Order Query](https://ibm-cloud-architecture.github.io/refarch-kc/microservices/order-query/)** microservice pages in the [Reefer Container Shipment reference implementation](https://ibm-cloud-architecture.github.io/refarch-kc/) gitbook.

---

## Build & Run

This project is built using the [Appsody](https://appsody.dev/) development framework. For a full understanding of Appsody applications, reference the Appsody [documentation](https://appsody.dev/docs) and [getting started](https://appsody.dev/docs/getting-started/) material respectively.

Specific deployment parameters are exposed in the `app-deploy.yaml` file for both [Order Command](https://github.com/ibm-cloud-architecture/refarch-kc-order-ms/blob/master/order-command-ms/app-deploy.yaml) and [Order Query](https://github.com/ibm-cloud-architecture/refarch-kc-order-ms/blob/master/order-query-ms/app-deploy.yaml).

For complete documentation on the necessary deployment configuration and parameters, reference the **[Order Command](https://ibm-cloud-architecture.github.io/refarch-kc/microservices/order-command/)** and **[Order Query](https://ibm-cloud-architecture.github.io/refarch-kc/microservices/order-query/)** microservice pages in the [Reefer Container Shipment reference implementation](https://ibm-cloud-architecture.github.io/refarch-kc/) gitbook.

---

## Contribute

As this reference implementation is part of the Event-Driven Architeture reference architecture, the overall [contribution policies](./CONTRIBUTING.md) apply here.

**Maintainers:**
* [Jerome Boyer](https://www.linkedin.com/in/jeromeboyer/)
* [Jesus Almaraz](https://www.linkedin.com/in/jesus-almaraz-hernandez/)
* [Rick Osowski](https://www.linkedin.com/in/rosowski/)

**Contributors:**
* [Edoardo Comar](https://www.linkedin.com/in/edoardo-comar/)
* [Jordan Tucker](https://www.linkedin.com/in/jordan-tucker-ba328a12b/)
* [Mickael Maison](https://www.linkedin.com/in/mickaelmaison/)
* [Francis Parr](https://www.linkedin.com/in/francis-parr-26041924)
