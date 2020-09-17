# Testchain

A simple blockchain written in Java

A block is automatically mined once the memory pool is full(2500 unconfirmed transactions were reached) or when the user types any input to the console
The block reward starts at 50 TC and halves while the difficulty increases after 25^x blocks mined, where x is the current difficulty target

For p2p node communication, server sockets are used alternatively JMS or RabbitMQ with separate queue's per node / Kafka with separate partitions&consumer groups are also valid choices

Many features like interactive charts rely on a database connection which I did not add because I could not find a proper key-value store and so I've rather preferred to use mappers and define proxy methods

![](Peek%202020-05-16%2000-34_2.gif)

## Getting Started


clone this repo

```
https://github.com/Dejanvuk/TestChain.git
```

### Installing


- Install module and its dependencies then build target jar
- Install the dependencies on frontend

```
mvn install -am -DskipTests
```

```
npm install
```

## Tests

The tests were designed for a node whose blocks can hold a maximum of 5 transactions and where the block reward doesnt halve, which made the tests very fast and easy to read, ~10 seconds in total, while 
essentially testing the same functionality as a node whose blocks can hold an X number of unconfirmed transactions and where the block reward halves at Y times.Otherwise handleTransactionTest would've taken
tens of minutes to complete which would've exponentially slowed the development.


## Running Tests

```
mvn test
```

## Deployment

Build docker images

```
docker build -t <name>/<frontend/backend-api> .
```

Run the images

You need to run the container with -t -i arguments and then simply docker attach <container-id> in order to send input to stdin of the blockchain container

```
docker run -t -i --name backend-api -p 8080:8080 -d <name>/backend-api
```

```
docker run -p 3000:80 <name>/frontend
```

## Built With

* [Spring](https://spring.io/projects/spring-framework) - The web framework used for blockchain
* [Maven](https://maven.apache.org/) - Dependency Management
* [ReactJS](https://reactjs.org/) - Framework of choice for the frontend
