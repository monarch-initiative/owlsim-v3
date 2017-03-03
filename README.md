# owlsim-v3

Ontology Based Profile Matching

[![Build Status](https://travis-ci.org/monarch-initiative/owlsim-v3.svg?branch=master)](https://travis-ci.org/monarch-initiative/owlsim-v3)
[![Coverage Status](https://coveralls.io/repos/monarch-initiative/owlsim-v3/badge.svg?branch=master&service=github)](https://coveralls.io/github/monarch-initiative/owlsim-v3?branch=master)
[![Documentation Status](https://readthedocs.org/projects/owlsim/badge/?version=latest)](http://owlsim.readthedocs.org/en/latest/?badge=latest)

This will eventually replace sim2 currently distributed as part of owltools

## Documentation

For now you must do this:

    mvn javadoc:javadoc

And then open target/site/apidocs/index.html in a browser

## Running it

To become more familiar with the code, please run the junit tests in eclipse.

Also try the performance/integration tests in `owlsim.compute.matcher.perf` - these will need extra files downloaded
see the Makefile for details.

To run the REST services:

    mvn package

produces a standalone Dropwizard jar:

    owlsim-services/target/owlsim-services-3.0-SNAPSHOT.jar

which runs with:

    cd owlsim-services
    java -jar target/owlsim-services-3.0-SNAPSHOT.jar server src/test/resources/test-configuration.yaml

where configuration.yaml describes the desired ontologies:

    ontologyUris:
      - owlsim-core/src/test/resources/simple-pheno-with-negation.owl
    ontologyDataUris: []

and then browse the [REST documentation](http://localhost:8080/api/docs/).

If you prefer to run the REST services from your IDE launch:
`org.monarchinitiative.owlsim.services.OwlSimServiceApplication` as a main
class with `server` and an appropriate YAML configuration as arguments.

Paths:

 * http://localhost:8080/api-docs/
 * http://localhost:8080/match/matchers -- lists matchers

or for some versions of dropwizard:

 * http://localhost:8080/api/docs/
 * http://localhost:8080/api/match/matchers

Example query using default config:

http://localhost:8080/api/match/jaccard?id=X:heart-morphology&id=X:brain-morphology

## Build with Docker

Run those commands from the root directory:

```
mvn package
docker build -t owlsim .
docker run -p 8080:8080 owlsim
```
