Farmers Bank: Keyclaok
============

Keycloak is an open source software product to allow single sign-on with Identity and Access Management aimed at modern applications and services

Building docker image
=========

Farmers bank keycloak runs on docker. To build the instance navigate to keycloak folder and eun

    docker build . -t keycloak

This will build a docker image using the `Dockerfile` in the keycloak directory.

To run use the following command

    docker run -d --name keycloak -p 9000:9000 \
        -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=Pa55w0rd \
        keycloak \
        start --optimized --https-port=9000 --log-level=DEBUG


Health check endpoints are available at https://localhost:9000/health, https://localhost:9000/health/ready and https://localhost:9000/health/live


Creating realm
=========

A realm manages a set of users, credentials, roles, and groups. A user belongs to and logs into a realm. Realms are isolated from one another and can only manage and authenticate the users that they control
