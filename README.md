# MobiLab Scalable Account Service
Account Management Service which will be the part of a bigger banking ecosystem. It means that this service will be consumed by other services in order to:
  - Define bank accounts with the given currency (EUR or USD) and make necessary changes on them if needed.
  - Perform money transactions between accounts with the functionality of currency conversion by using the exchange rates from a 3rd party API.
  - An additional endpoint for retrieving the transaction logs for a given time range. The endpoint returns the transactions ordered by date (ascending/descending).
  - This service starts getting a lot of traffic. Therefore one important objective is to scale up the application. The solution has been adjusited to be able to run service with multiple instances. 
  - Data consistency between instances has ben ensured.

# Prerequisites and  Technologies

  - Message Broker System→ Kafka
  - Micro Service design → Spring boot 
  - Data base→ MongoDB  (
  - Container based deployment → Docker
  - Bulk Technology
  - Junit and RestAssured
  - Maven
  

Here in below diagram you can see the big picture of project architecture and technologies that have been used in my design. 
We have two general process in this project: *Gateway* and *Business*.
![alt text](http://s9.picofile.com/file/8367050550/Screenshot_from_2019_07_18_13_39_22.png)


### Gateway Process
Gateway process is standing for receiving rest API requests and sending them into the broker system and also receiving their results. Generally, the Gateway process is not responsible for processing requests in business level. It has two main sup processes: request gateway and response gateway.
Request gateway is responsible for getting rest requests and generate MyRequest object and publish them to request topic. Response gateway receives responses are ready. 

Gateway process is not a heavy job because it doesn't have any thread and memory based object. Gateway is in a high level of scalability. so it will be enough to deploy it in one single instance.

### Bussiness Process
The business process is standing for handling business logic and performing queries and updates into the db. So each request which has been published into request topic by the Gateway will be processed here and corresponding response will be published into response topic.  
The business process can be executed in multiple instances to scale up this solution. I implemented a partitioning helper mechanism for the request topic in my code to make sure that all transactions for one accounts will be processed by the same business instance. In such design, there is no chance for race conditions in transactions like account creation, account update, deposit, and withdrawal. But for the transfer transaction, we should consider a solution. The business process is using mongo db functionalities to handle this problem. Let see an example of such a race condition. Process A wants to transfer 1000$ from one of its accounts to another account (Account No: 2) under the control of Business B. If in the same time Business B receives a request with withdrawal action form account No. 2 then we have a race condition situation. But business processes are not using set operation on their database update transactions. It means that Business A performs increment action and  Business B performs decrement action.
All the db transactions like insert, update and read will be done using bulk operation. As we know. each account will be processed by a specific business . We already find out that there is no chance for race condition for the updating account fields except the balance field. So the Business process will put several transactions in a list and execute them on a predefined period as a bulk operation. All the transactions will be applied in an ordered way based on their message timestamp. 

So we can say that there is no way for the gateway to updates accounts information directly. After processing a request in a business process, the business will generate a response for that request and publish it into response topic.  Then response gateway will consume the message and pass it to the rest. If rest module cannot find a response for a request then it will ask response gateway to fetch data directly from the database. So if a business process has consumed a request and made a change on db and then it would be broken, response gateway can fetch the desired result from the db. All messages have a configurable retention setting. It guarantees Kafka performance. 

We also have a re-balancer in this solution to support business processes failure. It means that all offsets will be managed by the re-balancer. Each business process makes a commit once it processed a message and this means that message will be removed from the request topic. 
Note that each transaction and transaction log will be done in one database transaction. 
Using Mongo DB is giving us the ability to handle transactions synchronization in DB level. We already talked about bulk updates but as you know we can also increase the scalability of our solution at DB level. But it needs more efforts and time. 

# Setup Instruction
First of all we need to install kafka and mongodb on the test machine.
### Kafka configuration
Then we need to create two topics on our kafka instance named by: 
*t_db_request* 
*t_db_response* 
Also we need to add our test machine IP address in kafka server config file → config/server.properties
for example:
*advertised.listeners=PLAINTEXT://your-ip-address:9092*
```sh
$ bin/zookeeper-server-start.sh config/zookeeper.properties
$ bin/kafka-server-start.sh config/server.properties 
```
### Mongo Configuration
We need to set bind_ip on mongo db configuration file. for example: 
*bind_ip = 192.168.1.186*

### Containers
Here you can see proposed containerizing env. for this solution. The optimized number of instances for business is equal to number of partitions in each topic. 


![containerizing env](http://s8.picofile.com/file/8367052126/Screenshot_from_2019_07_18_13_54_43.png)

##### Business Docker
```sh
$ docker build -f Dockerfile -t business 
```
Please run created image wih following arguments
```sh
b
--server-port=8090
--my.rest=false
--mongodb_host
--kafka.bootstrap.servers=
--zookeeper_host=
```

For example:
```sh
$ docker run -ti -p 8090:8090 business b --server.port=8090 --my.rest=false --mongodb_host=192.168.1.186:27017 --kafka.bootstrap.servers=192.168.1.186:9092 –zookeeper_host=192.168.1.186:2181
```
##### Gateway Docker
```sh
$ docker build -f Dockerfile -t gateway 
```
```sh
$ docker run -ti -p 80:8080 gateway --my.rest=true --mongodb_host=192.168.1.186:27017 --kafka.bootstrap.servers=192.168.1.186:9092 --zookeeper_host=192.168.1.186:2181
```

### Currency Exchange
I have used openexchangerates.org api for getting currency exchange rate. It will be cached for one day and after one day it would be refreshed. 

### Java Doc
You can find java doc files in doc directory of the project


