
The event storming workshop helped us to engage with the business experts and understand the end to end reefer shipping process as well as the events created. The following diagram represents part of this work: (It may have evolved a little bit as we worked with domain experts to learn about the domain)

![](images/event-storming-order.png)

We assume you are now familiar with the event storming methodology as presented in [this note](http://ibm-cloud-architecutre.github.io/refarch-eda/methodology/eventstorming.md). And you have some basic knowledge of domain driven design, from reading books like [Eric Evans's "Domain Driven Design: Tackling Complexity in the Heart of Software" book]( https://learning.oreilly.com/library/view/domain-driven-design-tackling).

We recommend to read the chapter ["From analysis to implementation"]() to get a clear understanding of the application design and the ubiquituous language of the reefer shipping domain.

In this part we focus on the order sub-domain.

![](images/order-subdomains.png)

The order subdomain interacts with the contract subdomain via the acceptance of the contract conditions from the customer (e.g. manufacturer) and by building a contract from the created shipment order. 

When the contract is accepted, the order needs to be shipped, but to do so the shipping subdomain needs to interact with the voyage subsystem to get the available vessel itineraries. Voyage is an entity grouping the information about the origin harbor close to the pickup address, to a destination harbor the closest to the shipping address. Finally, the shipping subdomain needs to interact with the container inventory service to get matching Reefer containers, present at the source harbor. 

## Ubiquitous language

### Entities and Value Objects

**Order** is the main business entity of this service, and is uniquely identified by its orderID. The orderID is sequentially created when persisting it in its repository. The OrderID will be communicated asynchronously to the customer via a confirmation email.

![](images/ent-vo-order.png)

The value objects:

* the different addresses
* delivery specifications
* deliver history

### Aggregate boundaries

In Domain-driven design, an aggregate groups related object as a unique entity. One object is the aggregate root. It ensures the integrity of the whole. Here the root is the Order. The product to ship is part of the aggregate. The Order is what we will persist in one unique transaction. 

The Reefer and Order and Voyage seem to be an obvious aggregates.

![](images/order-aggregate-boundary.png)

Customer and Vessets are also aggregates, but we will not consider them as scope for the solution implementation.

In this project we focus on the **shipping order** aggregate.

### Shipment order lifecycle and state change events 

The scoping decisions for the demonstration build listed above are reflected in a shipment order life cycle diagram shown below.

![order-life-cycle](images/order-life-cycle.png)

A shipment order is initially created with an API call made by a manufacturer, or via a user interface The order request specifies: 

* The pickup location where empty container will be loaded 
* The delivery location where the container is to be delivered to (we expect this to be in a remote country requiring a sea voyage) 
* The type of good with the target temperature to maintain a long the voyage.
* The shipment time window i.e.:  
    * Earliest date at which goods are available at pickup location for loading into the container 
    * Date by which delivery to the destination address is required   
Since our initial demonstration build expects to show refrigeration behavior and track preservation of a cold chain, we assume that orders are for some commodity which requires refrigeration during its shipment. 

When a new shipment order is placed, the shipping company must determine whether there is available capacity in some planned ship voyage which meets all the requirements specified by the manufacturer / customer. If there is a planned voyage with available capacity for additional container going from the source port nearest the pickup location to the destination port nearest to the delivery location then the order can transition to `state=BOOKED` and positive confirmation of the order returned to the requester. If no such voyage is available then the shipment order transitions to `state=REJECTED` (No Availability) and this is reported back to the requester. 

Once an order is `BOOKED`, then the expected dates and locations where for which a container will be needed are known. A request can be issued to book a specific (refrigerated) container for use with this shipment. We assume that the shipping company always has enough container available to meet expected shipment demand, hence the shipment order will transition to `state=CONTAINER_ALLOCATED` when this container booking is received. 

Since the scope for this demonstration build excluded the simulation of trucking operations to get the goods from the manufacturer's pickup location, export clearance and actual dockside loading operations, once an order has a container allocated it is "ready to go" and transitions to `state=FULL_CONTAINER_VOYAGE_READY`.  

The actual event of recording the container as being on board ship and at sea will not happen until simulated time in the demonstration reaches the scheduled start of the voyage on which that container is booked and the container ship assigned to that voyage is in the source port and also ready to go. At that point in simulated time, the state of the shipment order changes from `state = FULL_CONTAINER_VOYAGE_READY` to `state = CONTAINER_ON_SHIP`.

While the order has `state = CONTAINER_ON_SHIP`, then we will be receiving temperature information from the Container simulation and Ship position information from the ship simulation service. Both provide a continuous streaming souces of information which should be considered part of the extended shipment state. 

After some period of simulated time, the ship will reach the destination port of the voyage. At this time the order transitions to `state = CONTAINER_OFF_SHIP` since our scope excluded simulation of actual dockside unloading information. Since we are not modelling customs clearance or trucking operations, there are no further events to be modeled until the order `state = CONTAINER_DELIVERED`. 

Since we are not modelling invoicing and billing operations the Container can be deallocated from this order and returned to some pool of free containers. When that has occurred the order state can be considered `state = ORDER_COMPLETED`. 

We have described the nornal, exception-free path first. There are two exception cases modelled:  

* At the time a new shipment order is requested, there may be no voyage with available capacity meeting the location and time requirements of the request. When this occurs, the manufacturer/user is informed and the order state becomes `state= REJECTED` (No Availability). At this point, the user can modify the order with a second API requests changing dates or possibly locations. This retry request could still fail returning the order back to `state = REJECTED` ( No availability). Alternatively the changes in dates and location could be enough for an available voyage to be found. When this occurs the order will transition to `state = BOOKED` modified. 
* If an API call to modify an order is made and the order is in some state different from `state = REJECTED` (No availability), we reject the API request. There could be race conditions, the order is in the process of being assigned to a voyage, or complex recovery issues. What if the order is already in a container and at sea when a modify order is received ?  Full treatment of these complex business specific issues is out of scope and avoided by the state check in the modify order call API call
* We also model the exception condition when the refrigeration unit in a container fails or is misset or over loaded. If the temperature in the container goes outside the service level range for that shipment the goods must be considered spoiled.  The order will transition from `state = CONTAINER_ON_SHIP` to  `state = ORDER_SPOILED` (Temperature out of Range). Some complex business recovery such as compensating the customer and possibly scheduling a replacement shipment may be required. The details will be contract specific and outside the scope, but we do include the use of Streaming event container analytics to detect the spoilage and use rule based real-time /edge adjustments of the refrigeration gear to avoid spoilage in the demonstration simulation.

### Repositories

The Order aggregate has its own repository. 

### Command - Policies and Events

When adding commands and business policies to the discovered events, we were able to isolate the following command and events for the order context.

![](images/order-evt-cmd.png)

Command APIs will be provided to: 

* Place a new shipment order.   
* Track an existing order, to confirm its booking state or to resolve the actual location and status of the container in transit.   
* Modify an order request which could not be booked within the requested time window.  

## CRQS and event sourcing

It makes sense to use CQRS and separate out order tracking into a separate orders-query-ms since: 

* The demand for order tracking might have significantly more intense scalability needs than order commands. Orders are typically created once and changes state a handful of times. There could be many different users querying status of a particular orders independently and each requesting tracking multiple times for each order to determine if there is some delay expected.
* Order state tracking information should probably be organized by requesting customer NOT by order ID:   
    * since customers should be allowed to see status on their own orders but not on other customer's orders 
    * when the shipping company is tracking an order it is most frequently doing so on behalf of a specific customer  
With this approach orders-query-ms becomes a CQRS query service with internal state updated from the event backbone, and an order tracking API.

## User stories

The business requirements is presented in [this note](https://ibm-cloud-architecture.github.io/refarch-kc/analysis/readme/)

The following user stories are implemented in this project:

- [ ] As a shipping company manager, I want to get the current newly created order list so that I can create contract manually.
- [ ] As a shipping company manager, I want to get a specific order list knowing its unique identifier so that I can review the data and know the current status.
- [ ] As a shipping company manager, I want to update the status of an order
- [ ] As a shipping company manager, I want select one of the proposed voyages, so that I can optimize the vessel allocation, and satisfy the customer.
- [ ] As a shipping company manager, I want to review the containers allocated to the order because I'm curious
- [ ] As a shipping company manager, I want to modify pickup date and expected delivery date to adapt to customer last request
 
The selected voyage must be from a source port near the pickup location travelling to a destination port near the delivery location requested by the customer.  It must be within the time window specified by the customer in the order request.  The selected voyage must have free space available (capacity not previously assigned to other orders) to accomodate the number of containers specified by the customer in their shipment request. From a design point of view we can imagine there is an automatic system being able to perform this assignment. This is implemented in the [Voyage microservice](https://ibm-cloud-architecture.github.io/refarch-kc-ms)

!!! warning
    All the end user interactions are done in the user interface, in a separate project, but the order microservice supports the backend operations.