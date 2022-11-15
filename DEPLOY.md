Farmers Bank: System Deployment Guide
============

Requirements
============
* Java >= 17 (OpenJDK JVM is tested by our CI on Travis)
* Docker and Docker Compose

Instructions how to run build CBS docker image
============

Run the following command to build jar and add it to a docker image:

`./gradlew :fineract-provider:jibDockerBuild -x test`

Update .env file with secure 32 characters secrets (you can use [this tool](https://delinea.com/resources/password-generator-it-tool) to generate secure secrets)

To spin up the services in docker container run the following command in the project root directory:

`docker-compose --env-file ./env -f docker-compose-prod.yml up -d`


Instructions to build setup postgres master-slave streaming replication in docker container
============
1. Create the replicator user on master (please update the [password](https://delinea.com/resources/password-generator-it-tool)):
  - `docker exec -it fineract-fineractpostgresql-1 bash`
  - `psql -U root`
  - `CREATE USER replicator WITH REPLICATION ENCRYPTED PASSWORD '5JwKGPcZDJ$G@zIp';`
  
2. Create the physical replication slot on master
  `SELECT * FROM pg_create_physical_replication_slot('replication_slot_slave1');`

- To see that the physical replication slot has been created successfully, you could run this query `SELECT * FROM pg_replication_slots;` and you should see something like this.
  
  [ RECORD 1 ]-------+------------------------<br />
  slot_name           | replication_slot_slave1<br />
  plugin              |<br />
  slot_type           | physical<br />
  datoid              |<br />
  database            |<br />
  temporary           | f<br />
  active              | f<br />
  active_pid          |<br />
  xmin                |<br />
  catalog_xmin        |<br />
  restart_lsn         |<br />
  confirmed_flush_lsn |

3. Run `docker exec -it fineract-fineractpostgresql-1 pg_basebackup -D /backup/postgresslave -S replication_slot_slave1 -X stream -P -U replicator -Fp -R` to get a backup from our master database and restore it for the slave.

4. To copy backup from docker container to host machine `docker cp fineract-fineractpostgresql-1:/backup/postgresslave .`

