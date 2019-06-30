# Domain-driven design applied to order context

During the event storming analysis, we define the domain to be the container shipment domain. It groups a set of subdomains like orders, contract, shipping, and external systems as the voyage scheduling and the container inventory management:

![](domain-subdomains.png)

!!! note
    Notice that at this time just three physical systems exist. The grouping of the orders, contract and shipping sub domain in one boundary context may be considered as an analysis shortcut as we want to clearly separate those subdomain as the ownership and the ubiquituous language are differents. 

We have three subdomain and all can be considered core domains. They represent company's competitive advantages and directly impact the organization business.

![](domain-subdomains-2.png)

The order subdomain interacts with the contract subdomain via the acceptance of the contract conditions from the customer (e.g. manufacturer) and by building a contract from the created shipment order. 

When the contract is accepted, the order needs to be shipped, but to do so the shipping subdomain needs to interact with the voyage subsystem to get the available vessel voyages. Voyage is an entity grouping the information about the source harbor close to the pickup destination, to a target harbor the closest to the shipping destination. Finally, the shipping subdomain needs to interact with the container inventory service to get matching Reefer containers, present at the source harbor. 

## Bounded Contexts

Within a business context every use of a given domain term, phrase, or sentence, **the Ubiquitous Language** inside the boundary has a specific contextual meaning. So order context is a boundary context and groups order, ordered product type, pickup and shipping addresses. The business problem to address is, how to make order traceability more efficient so customers have a clear view of their orders.
 
An order will be assigned to one or many containers and containers are assigned to a voyage.

## Order aggregate

In Domain-driven design, an aggregate groups related object as a unique entity. One object is the aggregate root. It ensures the integrity of the whole. Here the root is the Order. The product to ship is part of the aggregate. The Order is what we will persist in one unique transaction. 

## Command - Policies and Events

When adding commands and business policies to the discovered events, we were able to isolate the following command and events for the order context.

![](order-evt-cmd.png)

From the command we can design our first APIs interface:

* Order createOrder(orderDTO)
* Order updateOrder(order)
* Order getOrders(manufacturerName)

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