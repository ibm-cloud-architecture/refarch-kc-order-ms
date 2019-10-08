# Reefer Container Shipment Order Management Service

!!! abstract
    This project is demonstrating, one of the possible implementation of the Command Query Responsibility Segregation and event sourcing patterns applied to Reefer shipment order subdomain. It is part of the Event Driven Architecture [solution implementation](https://ibm-cloud-architecture.github.io/refarch-kc). From a use case point of view, it implements the order management component, responsible to manage the full life cycle of a shipping order issued by a customer who wants to ship fresh goods overseas. The business process is defined [here](https://ibm-cloud-architecture.github.io/refarch-kc/introduction/) and the event storming analysis in [this note](https://ibm-cloud-architecture.github.io/refarch-kc/analysis/readme/). We are also presenting one way of applying Domain Drive Design practice for this subdomain.

## What you will learn

By studying this repository you will be able to learn the following subjects:

* How to apply domain driven design for a CQRS microservice
* How to adopt CQRS pattern for the shipping order management 
* How to apply ubiquituous language in the code
* Develop and deploy a microprofile 2.2 application, using open Liberty, on openshift or kubernetes

## Requirements

The key business requirements we need to support are:

* Be able to book a fresh product shipment order, including the allocation of the voyage and the assignment of a reefer container to the expected cargo.
* Be able to understand what happen to the order over time: 
    * How frequently does an order get cancelled after it is placed but before an empty container is delivered to pick up location or loaded ?
    * Track key issue or step in the reefer shipment process
    * How often does an order get cancelled after the order is confirmed, a container assigned and goods loaded into it?
* Be able to support adhoc query on the order that span across subdomains of the shipment domain. 
    * What are all events for a particular order and associated container shipment?  
    * Has the cold chain been protected on this particular order?
    * How long it takes to deliver a fresh food order from california to China?

Those requirements force use to consider event sourcing (understanding facts about the order over time) and CQRS patterns to separate queries from command so our architecture will be more flexible and may address different scaling requirements.  

## Applying Domain Driven Design

We are detailing, in a [separate note](ddd-applied.md), how to go from the event storming produced elements to the microservice implementation by applying the domain-driven design approach.

## Review detail implementation considerations

The code is the source of truth, but we are providing some simple explanations on how to navigate into the code, and some implementation consideration in [this note](implementation-considerations.md).

## Deploy to kubernetes

See [this note](deployments.md) on how to deploy this service with its configuration on Openshift or kubernetes.