# Reefer Container Shipment Order Management Service

!!! abstract
    This project is demonstrating, one of the possible implementation of the Command Query Responsibility Segregation and event sourcing patterns applied to Reefer shipment order subdomain. It is part of the Event Driven Architecture [solution implementation](https://ibm-cloud-architecture.github.io/refarch-kc). From a use case point of view, it implements the order management component, responsible to manage the full life cycle of a shipment order issued by a manufacturer who wants to ship fresh goods overseas. The business process is defined [here](https://ibm-cloud-architecture.github.io/refarch-kc/introduction/). We are also presenting one way of applying Domain Drive Design practice for this subdomain.

The key business requirements we need to support are:

* Be able to book a fresh product shipment order, including the allocation of the voyage and the reefer container to the expected cargo.
* Be able to understand what happen to the order over time: 
    * How frequently does an order get cancelled after it is placed but before an empty container is delivered to pick up location or loaded ?
    * Track key issue or step in the reefer shipment process
    * How often does an order get cancelled after the order is confirmed, a container assigned and goods loaded into it?
* Be able to support adhoc query on the order that span across subdomains of the shipment domain. 
    * What are all events for a particular order and associated container shipment?  
    * Has the cold chain been protected on this particular order?
    * How long it takes to deliver a fresh food order from california to China?

Those requirements force use to consider event sourcing (understanding facts about the order over time) and CQRS patterns to separate queries from command so our architecture will be more flexible and may address different sclaing requirements.  

## Applying Domain Driven Design

We are detailing, in a [separate note](ddd-applied.md), how to go from the event storming produced elements to the microservice implementation by applying the domain-driven design approach.

## Implementation considerations

As introduced in the [solution high level design note](https://ibm-cloud-architecture.github.io/refarch-kc/design/readme/) the order entity life cycle looks like in the following diagram:

![](order-life-cycle.png)

The order microservice supports the implementations of this life cycle, using event sourcing and CQRS pattern.

With [CQRS](https://ibm-cloud-architecture.github.io/refarch-eda/design-patterns/cqrs/), we separate the 'write model' from the 'read model'. The Command microservice implements the 'write model' and exposes a set of REST end points for Creating Order, Updating, Deleting Order and getting Order per ID. The query service will address more complex queries to support adhoc business requirements and most likely will need to join data between different entities like the order, the containers and the voyages. So we have two Java projects to support each service implementation. Each service is packaged as container and deployable to Kubernetes. 

* [Order command microservice](https://github.com/ibm-cloud-architecture/refarch-kc-order-ms/tree/master/order-command-ms)
* [Order query microservice](https://github.com/ibm-cloud-architecture/refarch-kc-order-ms/tree/master/order-query-ms)

As some requirements are related to historical query, using an event approach, we need to keep all the events related to what happens to the order. Instead of implementing a complex logic with the query and command services, the event sourcing is supported by using Kafka topics. The following diagram illustrates the CQRS and event sourcing applied to the order management service. Client to the REST api, like a back end for front end app, performs a HTTP POST operation with the order data. The command generates events and persists order on its own data source. The query part is an event consumer and defines its own data projections to support the different queries:

![](order-ms-cqrs-es.png) 

The datasource at the command level, may not be necessary, but we want to illustrate here the fact that it is possible to have a SQL based database or a document oriented database to keep the order last state: a call to get /orders/{id} will return the current order state. 

For the query part the projection can be kept in memory or persisted on its own data store. The decision, to go for in memory or to use a database, depends upon the amount of data to join, and the persitence time horizon set at the Kafka topic level. In case of problem or while starting, an event driven service may always rebuild its view by re-reading the topic from the beginning. 

!!! note
    An alternate solution is to have the BFF pushing events to the event source and then having the order service consuming event to persist them, as illustrated in the following diagram:

    ![](bff-es-cqrs.png)

As the BFF still needs to get order by ID or perform complex queries, it has to access the order service using HTTP, therefore we have prefered to use one communication protocol.  

The following sequence diagram illustrates the relationships between the components over time:

![](order-cmd-query-flow.png)

To avoid transaction between the database update and the event published, the choice is to publish the event as early as it is received and use a consumer inside the command service to load the data and save to the database. The kafka topic act as a source of trust for this service. This is illustrated in [this article.](https://ibm-cloud-architecture.github.io/refarch-eda/evt-microservices/ED-patterns/#the-consistency-challenge)

The /orders POST REST end point source code is [here](https://github.com/ibm-cloud-architecture/refarch-kc-order-ms/blob/6de424c443c05262ae013620f5f11b4a1b2e6f90/order-command-ms/src/main/java/ibm/labs/kc/order/command/service/OrderCRUDService.java#L51-L74)

and the [order events consumer](https://github.com/ibm-cloud-architecture/refarch-kc-order-ms/blob/6de424c443c05262ae013620f5f11b4a1b2e6f90/order-command-ms/src/main/java/ibm/labs/kc/order/command/service/OrderAdminService.java#L35) in the command pattern.

See the class [OrderCRUDService.java](https://github.com/ibm-cloud-architecture/refarch-kc-order-ms/blob/master/order-command-ms/src/main/java/ibm/labs/kc/order/command/service/OrderCRUDService.java).
* Produce order events to the `orders` topic. 
* Consume events to update the state of the order or enrich it with new elements.

When the application starts there is a [ServletContextListener](https://docs.oracle.com/javaee/6/api/javax/servlet/ServletContextListener.html) implementation class started to create a kafka consumer and to subscribe to order events (different types). When consumer reaches an issue to get event it creates an error to the `errors` topic, so administrator user could replay the events from the last committed offset. Any kafka broker communication issue is shutting down the consumer loop.

## Data and Event Model

By applying a domain-driven design we can identify aggregates, entities, value objects and domain events. Those elements help us to be our information model as classes. For any event-driven microservice you need to assess what data to carry in the event and what persist in the potential data source. 
The following diagram illustrates the different data models in the context of this order microservice:

![](./order-evt-data.png)

The Order entered in the User interface is defined like:
```
 class Address {
    street: string;
    city: string;
    country: string;
    state: string;
    zipcode: string;
}

 class Order {
    orderID: string;
    customerID: string;
    pickupAddress: Address;
    destinationAddress: Address;
    productID: string;
    quantity: string;
    expectedDeliveryDate: string;   //  date as ISO format
}
```

The information to persist in the database may be used to do analytics, and get the last status of order. It may look use relational database and may have information like:

```
 class Address {
    street: string;
    city: string;
    country: string;
    state: string;
    zipcode: string;
}

 class Order {
    orderID: string;
    customerID: string;
    pickupAddress: Address;
    destinationAddress: Address;
    productID: string;
    quantity: string;
    expectedDeliveryDate: string;   //  date as ISO format
    pickupDate: string;   //  date as ISO format
}

class OrderContainers {
    orderID: string;
    containerID: string[];
}
```

On the event side we may generate OrderCreated, OrderCancelled,... But what is in the event payload? 

We can propose the following structure where type will help to specify the event type and by getting a generic payload we can have anything in it.

```
class OrderEvent {
    orderId: string;
    timestamp: string;   //  date as ISO format
    payload: any;
    type: string;
    version: string;
}
```

`version` attribute will be used when we will use a schema registry.

There are other questions we need to address is real project:

* do we need to ensure consistency between those data views? 
* Can we consider the event, which are immutable data elements, as the source of truth? 

In traditional SOA service with application maintaining all the tables and beans to support all the business requirements, ACID transactions support the consistency and integrity of the data, and the database is one source of truth. With microservices responsible to manage its own aggregate, clearly separated from other business entities, data eventual consistency is the standard. If you want to read more about the Event Sourcing and CQRS patterns [see this article.](https://ibm-cloud-architecture.github.io/refarch-eda/evt-microservices/ED-patterns)

