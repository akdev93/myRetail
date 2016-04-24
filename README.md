# myRetail

This repository contains all the artifacts to build an aggregation service to vend product infromation. The product information consists of the catalog information and the current selling price of the product both of which are compiled from disparate sources. The catalog information is obtained from an external API while the pricing information is looked up from a NoSQL data store.  The service also contains API to update the price to a provided value after performing validations on the input.

## Assumptions Made

Below are the assumptions made in implementing the APIs contained in the service

- The service is an authority on pricing. It makes the following validations to ensure data quality
  - The price cannot be negative
  - The price will accepted if and only if there is catalog information for the product. If there is no catalog information, the price update will fail (Refer API documentation)
  - The resource identifier for the price should match the payload
- The service is has an aggregation function to aggregate data from the catalog service, but does not acts an authority for the catalog data. No validation are peformed on the productId
- Only currency code supported is "USD" although the implementation beyond the API layer can support multiple currency codes

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



|API      | `/myRetailApi/product/{id}`|
|Response |

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
|
## Design Decisions

## Components

