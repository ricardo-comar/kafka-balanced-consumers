


## Setting up the outside resources

### Kafka cluster and topics creation

First, start the resources with docker-compose:
```
docker-compose up
```

You should see a lot of messages, related to the first time instances creation and running. 

At end they should be all running. You can list the services using "docker ps -a" and test the services by trying to connect with telnet to each port (3306, 12181, 22181, 32181, 19092, 29092, 39092).

```
CONTAINER ID        IMAGE                              COMMAND                  CREATED             STATUS                    PORTS                                                    NAMES
bf149d33b65d        confluentinc/cp-kafka:latest       "/etc/confluent/dock…"   17 hours ago        Up 2 minutes              9092/tcp, 0.0.0.0:19092->19092/tcp                       kafka-balanced-consumers_kafka-1_1
bd175d847b55        confluentinc/cp-kafka:latest       "/etc/confluent/dock…"   17 hours ago        Up 2 minutes              9092/tcp, 0.0.0.0:29092->29092/tcp                       kafka-balanced-consumers_kafka-2_1
76229db017ba        confluentinc/cp-kafka:latest       "/etc/confluent/dock…"   17 hours ago        Up 2 minutes              9092/tcp, 0.0.0.0:39092->39092/tcp                       kafka-balanced-consumers_kafka-3_1
76da9a64dd03        confluentinc/cp-zookeeper:latest   "/etc/confluent/dock…"   17 hours ago        Up 2 minutes              2181/tcp, 2888/tcp, 3888/tcp, 0.0.0.0:12181->12181/tcp   kafka-balanced-consumers_zookeeper-1_1
8e3cba64ccf1        confluentinc/cp-zookeeper:latest   "/etc/confluent/dock…"   17 hours ago        Up 2 minutes              2181/tcp, 2888/tcp, 3888/tcp, 0.0.0.0:22181->22181/tcp   kafka-balanced-consumers_zookeeper-2_1
87c1acbb1472        confluentinc/cp-zookeeper:latest   "/etc/confluent/dock…"   17 hours ago        Up 2 minutes              2181/tcp, 2888/tcp, 3888/tcp, 0.0.0.0:32181->32181/tcp   kafka-balanced-consumers_zookeeper-3_1
fb95dd92017d        mysql:latest                       "docker-entrypoint.s…"   17 hours ago        Up 2 minutes              0.0.0.0:3306->3306/tcp, 33060/tcp                        kafka-balanced-consumers_db_1

```
Now we need to identify the zookeeper leader instance. Connect with telnet to the first zookeeper instance (port 12181) and send the command "stats". If it's not the leader, it will reply with "Mode: follower".  Try the next port (22181 or 31181) until you receive a "Mode: leader".

In my example I'll pick the first instance (port 12181) as the leader, and will connect to it with "docker exec -it 76da9a64dd03 /bin/bash", to setup the topics. Then, send this command to list the available topics: 

```
kafka-topics --zookeeper zookeeper-1:12181 --list
```
Now you should see only two topics, used by kafka to keep things running. You can check their information by asking to describe them:
```
kafka-topics --zookeeper zookeeper-1:12181 --describe 
```
Now, let's create our topics. Run these two lines:
```
kafka-topics --zookeeper zookeeper-1:12181 --create --topic topicOutbound --partitions 6 --replication-factor 3
kafka-topics --zookeeper zookeeper-1:12181 --create --topic topicInbound --partitions 6 --replication-factor 3
```
And now check if they are created as expected:
```
kafka-topics --zookeeper zookeeper-1:12181 --describe
```

# Running all together



# References

* https://better-coding.com/building-apache-kafka-cluster-using-docker-compose-and-virtualbox/
* https://spring.io/guides/gs/rest-service/
* https://github.com/edenhill/kafkacat
* https://dzone.com/articles/magic-of-kafka-with-spring-boot
* https://www.baeldung.com/spring-kafka
* https://docs.spring.io/spring-kafka/reference/html/#kafka
* https://thepracticaldeveloper.com/2018/11/24/spring-boot-kafka-config/
* https://dzone.com/articles/20-best-practices-for-working-with-apache-kafka-at
* https://www.baeldung.com/java-concurrent-locks
* https://cloud.spring.io/spring-cloud-netflix/multi/multi__router_and_filter_zuul.html
* https://www.baeldung.com/spring-cloud-netflix-eureka
* https://www.baeldung.com/zuul-load-balancing


kafkacat -C -b kafka-1:29092 -t topicInbound
kafkacat -C -b kafka-1:29092 -t topicOutbound


http://localhost:8080/eureka/apps
http://localhost:8080/app/producer/actuator/health

