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

## Installation Instructions



