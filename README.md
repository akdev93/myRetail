# myRetail

This repository contains all the artifacts to build an aggregation service to vend product infromation. The product information consists of the catalog information and the current selling price of the product both of which are compiled from disparate sources. The catalog information is obtained from an external API while the pricing information is looked up from a NoSQL data store.  The service also contains API to update the price to a provided value after performing validations on the input.


## Assumptions

Below are the assumptions made in implementing the APIs contained in the service

- The service is an authority on pricing. It makes the following validations to ensure data quality
  - The price cannot be negative
  - The price will accepted if and only if there is catalog information for the product. If there is no catalog information, the price update will fail (Refer API documentation)
  - The resource identifier for the price should match the payload
- The service is has an aggregation function to aggregate data from the catalog service, but does not acts an authority for the catalog data. No validation are peformed on the productId
- Only currency code supported is "USD" although the implementation beyond the API layer can support multiple currency codes
- Only complete product information should be vended by the service. If either catalog or price is missing, it should be treated as if the product was not found. _(In complete or defaulted information should not be returned)_

## Technical Details
### Technologies and APIs used

1.  NoSQL data store : Cassandra (datastax-ddc-3.5.0)
2.  Catalog API : A Mock service which serves the use case hosted at https://catalogservice-1291.appspot.com/api/products/v3. _The mock service vends the catalog id and the name_

### Installation and Set up

To install and run the service you will either need an existing cassandra instance or set up a new cassandara database. The instructions assume you will need to set up a new cassandra instance, but you can skip the steps if you decide to use an existing cassandra instance. 

#####Cassandra Installation 

######Prerequisites
1. Java 8 (Oracle Java Platform, Standard Edition 8 (JDK))
2. Python 2.6+


######Installation Instructions
1. Download the tarball for cassandra 3.5 [datastax-ddc-3.5.0](https://downloads.datastax.com/datastax-ddc/datastax-ddc-3.5.0-bin.tar.gz)
2. Untar the tarball in a directory of your choice ($CASSANDRA_ROOT)
3. This should create a  directory structure rooted at $CASSANDRA_ROOT/datastax-ddc-3.5.0
4. Update the environment to set the CASSANDRA_HOME to $CASSANDRA_ROOT/datastax-ddc-3.5.0
5. Create the necessary data directories that is used by cassandra

```
   $>export CASSANDRA_HOME=$CASSANDRA_ROOT/datastax-ddc-3.5.0;
   $>mkdir -p $CASSANDRA_HOME/data
```

######Start Cassandra

```
   $>cd $CASSANDRA_HOME/bin
   $>./cassandra
```
######Database set up

The steps are explained below. It is assumed that you have cassandra running at this time.

```
   $> cd $CASSANDRA_HOME/bin
   $> ./csqlsh
   $>
   cqlsh> -- Create a keyspace called 'product_test'
   cqlsh> create KEYSPACE if not EXISTS product_test with replication = {'class': 'SimpleStrategy', 'replication_factor' : 3};
   cqlsh>
   cqlsh>
   cqlsh> -- lets use the keyspace and set up the objects we need
   cqlsh> use product_test;
   cqlsh>
   cqlsh>
   cqlsh:product_test> -- create the column family
   cqlsh:product_test> create table product_price (product_id text, currency_code text, selling_price float, primary key(product_id, currency_code));
   cqlsh:product_test>
   cqlsh:product_test>
   cqlsh:product_test> -- create some data
   cqlsh:product_test>insert into product_price(product_id, currency_code , selling_price) values ('15117729', 'USD', 11.01);
   cqlsh:product_test>insert into product_price(product_id, currency_code , selling_price) values ('16483589', 'USD', 11.02);
   cqlsh:product_test>insert into product_price(product_id, currency_code , selling_price) values ('16696652', 'USD', 11.03);
   cqlsh:product_test>insert into product_price(product_id, currency_code , selling_price) values ('16752456', 'USD', 11.04);
   cqlsh:product_test>insert into product_price(product_id, currency_code , selling_price) values ('15643793', 'USD', 11.05);
   cqlsh:product_test>
   cqlsh:product_test>-- all done. lest get out of here
   cqlsh:product_test>quit
   $>
```

######Service set up 

######Prerequisites
1. Java 8 (Oracle Java Platform, Standard Edition 8 (JDK))
2. Maven 3.3.3+

######Installation Instructions

- Clone this repository in a convinient directory ($CODE_ROOT)

```
   $>cd $CODE_ROOT
   $> git clone https://github.com/akdev93/myRetail.git
```

- To configure the service follow the steps below
  - Open `$CODE_ROOT/myRetail/myRetailApi/src/main/resources/myRetailApi.properties`
  - Update the properties below for your environment

```
PricingDAO.connectHost=localhost     # Cassandra connect host
PricingDAO.connectPort=9042          # Cassandra port
PricingDAO.keyspaceName=product_test # Keyspace name
```

  - Open `$CODE_ROOT/myRetail/myRetailApi/src/test/resources/test.properties`
  - Update the properties below for your environment

```
PricingDAO.connectHost=localhost     # Cassandra connect host
PricingDAO.connectPort=9042          # Cassandra port
PricingDAO.keyspaceName=product_test # Keyspace name
```

- To run the service 
```
  $> cd $CODE_ROOT/myRetail/myRetailApi
  $> mvn tomcat7:run
```

- Open a browser and  access the url `http://localhost:8080/myRetailApi/product/16696652` - you should see a JSON with the details of the product

## API documentation


#### API : (GET) `/myRetailApi/product/{id}`  

This API returns the product infromation which includes the catalog information and the associated price in USD where `id` is the identifier of the product 


##### Request Payload

This API does not expect a payload


##### Response

```
    {
      "id": "16696652",
      "name": "product 3",
      "current_price": {
        "value": 11.03,
        "currency_code": "USD"
      }
    }
```

|Attribute       | Description                                     |
|----------      |------------                                     |
|`id`            | Identifier for the product                      |
|`name`          | Short description of the product                |
|`current_price` | JSON object containing the price of the product |
|`value`         | Current price                                   |
|`currency_code` | Currency code associated with the current price |



|Status Code     | Description                                     |
|----------      |------------                                     |
|`404`           | Product not found                               |
|`500`           | Application Error (Could not process request)   |
|`200`           | Processing successful. Should generate the product information |



#### API : (PUT) `/myRetailApi/product/{id}`  

This API accepts a payload that is the same structure as the response to the GET and updates the price in the pricing database. The following validations are done in processing the request 

- The `id` element in the path  should match the `id` in the  payload
- The price should be greater than or equal to 0
- The catalog information should be available to accept the price


##### Request Payload


```
    {
      "id": "16696652",
      "name": "product 3",
      "current_price": {
        "value": 11.03,
        "currency_code": "USD"
      }
    }
```

|Attribute       | Description                                     |
|----------      |------------                                     |
|`id`            | Identifier for the product                      |
|`name`          | Short description of the product                |
|`current_price` | JSON object containing the price of the product |
|`value`         | Current price                                   |
|`currency_code` | Currency code associated with the current price |


##### Response

```
    {
      "id": "16696652",
      "name": "product 3",
      "current_price": {
        "value": 11.03,
        "currency_code": "USD"
      }
    }
```

|Attribute       | Description                                     |
|----------      |------------                                     |
|`id`            | Identifier for the product                      |
|`name`          | Short description of the product                |
|`current_price` | JSON object containing the price of the product |
|`value`         | Current price (After the udpate)                |
|`currency_code` | Currency code associated with the current price |



|Status Code     | Description                                     |
|----------      |------------                                     |
|`404`           | Product not found                               |
|`400`           | if ID in teh path does not match the id in the payload or if the price is < 0   |
|`200`           | Processing successful. Should generate the product information |
|`500`           | Application Error (Could not process request)   |


## Design Decisions

|ID  |Decision|Rationale|
|----|--------|---------|
|1 | The aggregation of the price and catalog information should be multi threaded | The lookup of the price does not depend on any information from the catalog retrieval. Hence, it can be done indepedently and parallel with the catalog fetch. This will save a couple of milliseconds on every request. This will help especially in high traffic low latency situations|
|2 | The primary key for the table in cassandra should include product id and currency code | If in future we support multiple currencies and need to show prices in all currencies, the price lookup should not cause read against multiple partitions. By creating a composite primary key, all records for the product are stored in the same partition|
|3 | Use JAX-RS to implment the REST API | Allows changes in provider with no change to the code. We use the JAX-RS reference implementation in Jersey |

## Design

The design uses an aggregator that multithreads the catalog fetch and the price lookup. Below are the components and their roles and their interaction

| Component |  Role |
|-----------|-------|
|ProductResource | Is the entry point for every API. It implements the REST fascade to vend or update information using the relevant verbs and http status codes following REST principles|
|ProductInfoAggregator | uses a thread pool to aggregate information from the catalog service and the pricing database. Processes updates to the pricing database |
|CatalogServiceProxy   | An Abstraction for the catalog service. A concrete implementation `RESTCatalogServiceProxyImpl` is used to interact with the catalog service|
|PricingDAO  | An abstraction for accessing pricing data. A concrete implementation `CassandraPricingDAO` is used to lookup the pricing from the cassandra pricing DB|
|AsyncDataAccess | An abstraction that provides the functionality to fetch data asynchronously. Both the `CatalogServiceProxy` as well as `PricingDAO` extend this abstraction|


```

     User Request   ---(GET)--->  ProductResource ---->  ProductInfoAggregator ---|---> CatalogInfoProxy -----> (External service for the catalog information)
                                  (REST Resource)                                 |
                                                                                  +---> PricingDAO       -----> (Cassandra db to lookup price)
```

The update of price does not use any multi threading. The request is validated by `ProductResource` and delegated to `ProductInfoAggregator` which processes the pricing using the `PricingDAO`

```

     User Request   ---(PUT)--->  ProductResource ---->  ProductInfoAggregator ---|---> PricingDAO  ----> (Cassandra db to lookup price)
                                  (REST Resource)                                 
```
