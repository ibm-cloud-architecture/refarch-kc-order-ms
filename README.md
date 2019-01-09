# K Container Shipment Order Management

This project is one reference implementation of the CQRS and event sourcing patterns as part of the [Event Driven Architecture](https://github.com/ibm-cloud-architecture/refarch-eda) reference architecture. From a use case point of view, it implements one of the [K Container shipment process](https://github.com/ibm-cloud-architecture/refarch-kc) microservices. This repository aims to support the order management for the manufactorer to the shipment company order. The business process is defined [here](https://github.com/ibm-cloud-architecture/refarch-kc/blob/master/analysis/readme.md).

## User stories to support

- [ ] As a shop manager I want to enter order information like product reference, quantity, shipment address and expected delivery date so the manufacturer can give me back an order confirmation and confirmed delivery date range.
- [ ] As a manufacturer manager I want to list the newly created orders, and confirm an order and set delivery date for that order once I received the confirmation from the shipment supplier, I will update the product cost.
- [ ] As a shipment company manager I want to see the order I can help to ship the goods to target address.  

## Event Sourcing

The pattern is well described in [this article on microservices.io](https://microservices.io/patterns/data/event-sourcing.html). To summarize evnt sourcing persists the states of business entity as a sequence of state changing events. Applications reconstruct the business entity current state by replaying the events. It is a very important pattern for EDA and microservices to microservices data synchronization needs. A view of entity states is build from the events or facts. It adds the value to provide a reliable audit log.

See also this nice [event sourcing article](https://martinfowler.com/eaaDev/EventSourcing.html) from Martin Fowler, where he is also using ship movement example. Our implementation differs here and we are using Kafka topic as event store and use the Order business entity.

## Command Query Responsibility Segregation (CQRS) pattern

This pattern helps to address how to implement a query to retrieve data from different microservices?

The CQRS pattern was introduced by [Greg Young](https://www.youtube.com/watch?v=JHGkaShoyNs), https://martinfowler.com/bliki/CQRS.html https://microservices.io/patterns/data/cqrs.html



## How to build and run

## How we implemented it

## Contribute

As this implementation solution is part of the Event Driven architeture reference architecture, the [contribution policies](./CONTRIBUTING.md) apply the same way here.

**Contributors:**
* [Jerome Boyer](https://www.linkedin.com/in/jeromeboyer/)

