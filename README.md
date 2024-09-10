## Index
1. Project Overview
2. Project Explanation (code and so on)
3. Run the app

## Overview
In this project, I am showcasing how to create a Spring app with Apache Kafka messaging queue using Protobuf as 
serializer and arbitrary field-level encryption. <br>
I am also using Confluent Schema Registry to upload the proto file which is then retrieved by the producer for the 
serialization of the message and by the consumer for the deserialization.

If you're unfamiliar with Protobuf, please check this straight-forward [introduction](https://protobuf.dev/) by Google.
If you're unfamiliar with Schema Registry, please feel free to check out this [introduction](https://docs.confluent.io/platform/current/schema-registry/index.html) by Confluent.

### Tech Stack
* Java 11
* Sprint Boot 2.7.18
* Spring Cloud Stream Kafka 3.2.6
* Protobuf
* gRPC

### Important Disclaimer
Please note the project goal is not to be production-ready but to showcase how to achieve a given result.
For time constraints, I have decided to skip the unit and integration tests of this project.
To find out what's my testing strategy and style, please refer to more complete projects on my GitHub Profile, such as [spring-cloud-stream-kafka-producer](https://github.com/MarcoFaccani/spring-cloud-stream-kafka-v3-producer) and [spring-cloud-stream-kafka-consumer](https://github.com/MarcoFaccani/spring-cloud-stream-kafka-v3-consumer). Also my [AWS Cloud projects](https://github.com/MarcoFaccani/spring-aws-s3) are fully tested. <br>
Thank you.

## Project Explanation
The flow is simple. The app exposes a gRPC endpoint. By calling that endpoint, the incoming protobuf message will
be serialized by a custom Serializer (`EncryptedProtobufSerializer`) which extends the Confluent `KafkaProtobufSerializer`.
During the serialization, it will identify which fields in the proto message have been tagged with the option "`encrypt`"
and will proceed with the encryption of these values by using the `EncryptionService`. <br>
Let's dive into the code.

### Custom Protobuf Serializer with Field-Level Encryption
First off, we need to add the dependency contains the Confluent KafkaProtobufSerializer.
Here is the configuration for maven:
```
<repositories>
  <repository>
    <id>confluent</id>
    <url>https://packages.confluent.io/maven/</url>
  </repository>
</repositories>

<dependency>
  <groupId>io.confluent</groupId>
  <artifactId>kafka-protobuf-serializer</artifactId>
  <version>${kafka-protobuf-serializer.version}</version>
</dependency>
```
> Since the dependency is not available on the maven registry but only on the confluent one, we must add the repository to our bom/pom.

#### KafkaProtobufSerializer - how does it work?
Confluent's KafkaProtobufSerializer works in a way that makes interaction with the Schema Registry very smooth:
* **Automatic Schema Association**: when you send a message using the `KafkaProtobufSerializer`, it will:
1. Compare the Protobuf message you are serializing with the schemas already registered on Schema Registry.
2. If it finds a schema that matches the message, use that schema.
3. If it doesn't find a schema that matches, the schema is automatically registered.
> By default, it uses `io.confluent.kafka.serializers.subject.TopicNameStrategy` as a strategy to determine the name of 
> the subject in the Schema Registry. What it does is it will use the nape of the Kafka topic as subject query param to retrieve the appropriate schema. You can verify this in the logs as the `KafkaProtobufSerializer` configuration is printed.

<br> 

* **Schema Versioning**: each time you register a new schema to Schema Registry, a new version is assigned.
The serializer automatically retrieves and uses the latest version of the registered compatible schema for the specified subject.

### EncryptedProtobufSerializer - Custom Serializer with Field-Level Encryption
With this class, we are extending the `KafkaProtobufSerializer` by Confluent by adding a field-level encryption logic.
We iterate through each field of the proto message and verify if it is tagged with the option "`encrypt`".<br>
If it is, we encrypt the value and set it back in the proto message. Once we have iterated through all the fields, we delegate
to the `KafkaProtobufSerializer` the serialization of the message.
> Please note, the encryption logic isn't production ready or anything like it. The encryption logic (how to achieve encryption) is not the focus of this project.

#### About the custom tag "encrypt"
In protobuf is possible to define any custom tag in a straight-forward manner. Here is the extracted relevant section from the proto file of this project:
```
extend google.protobuf.FieldOptions {
  bool encrypt = 50001;
}

message GreetingRequest {
  string firstname = 1;
  string lastname = 2 [(encrypt) = true];
}
```
To refer to this tag (or any) in the source code, use the value of the `<java_package>.<java_outer_classname>.<custom-option-name>`.
In this scenario, the value would be `com.marcofaccani.grpc.server.v1.MyGrpcServer.encrypt`

### Configure Spring Cloud Stream Kafka via properties
```
spring:
  cloud:
    stream:
      bindings:
        greetingProducer-out-0:
          group: ${CONSUMER_GROUP_NAME:dummy-group}
          destination: kafka-protobuf-spike
          content-type: application/x-protobuf
          producer:
            use-native-encoding: true
      kafka:
        binder:
          auto-create-topics: true
          min-partition-count: 1
          brokers: ${KAFKA_BROKERS:<kafka-broker-ip-address>:9092}
          producer-properties:
            key.serializer: org.apache.kafka.common.serialization.StringSerializer
            value.serializer: com.marcofaccani.kafka.protobuf.field.encryption.channel.outbound.EncryptedProtobufSerializer
            schema.registry.url: http://localhost:8081
            value.subject.name.strategy: io.confluent.kafka.serializers.subject.TopicNameStrategy
```
Worth of noting:
* `use-native-encoding: true` – needed to use a custom Serializer
* `value.serializer: com.marcofaccani.kafka.protobuf.field.encryption.channel.outbound.EncryptedProtobufSerializer` - here we are configuring Spring Cloud to use our custom Serializer 
* `schema.registry.url: http://localhost:8081`
* `content-type: application/x-protobuf`


## Running the App
In order to run the app and perform an E2E local test, we need to set up `Kafka` and `Confluent Schema Registry`, 
as well as having a gRPC client (Postman works just fine, otherwise you can use gCurl)

### Setup Kafka
I have kafka installed on my machine but feel free to use the appropriate docker image if you don't have it locally.
No need to create the topic manually as the app is responsible for doing it.
Please make sure the Kafka Broker is listening on port `9092`, otherwise change the related value in `resources/application.yml`.

### Setup Confluent Schema Registry
The docker image to use is named `confluentinc/cp-schema-registry:latest`.<br>
Here is the handy command to run it:
```
docker run   \
  -p 8081:8081 \
  -e SCHEMA_REGISTRY_HOST_NAME=<any-name> \
  -e SCHEMA_REGISTRY_KAFKASTORE_ZK_CONNECT=<kafka-zookeeper-ip-address>:<port> \
  -e SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS=<kafka-broker-ip-address>:<port> \
  -e SCHEMA_REGISTRY_LISTENERS=http://0.0.0.0:8081 \
  confluentinc/cp-schema-registry:latest
```
> If you are running Kafka through a local install, please note you have to use your local IP address assigned from your router
> and configure the `config/server.properties` file of Kafka to use that same IP address.
> For example, if your local IP Address is 192.150.1.10, then you should write the following: `listeners=PLAINTEXT://192.150.1.10:9092`
> This is the only way I managed to have a docker container reach out to a service running on my machine. 
> Probably there are easier ways and I will dive into it when I will have more time. If you have any suggestions I would love to hear about them! :)

#### Uploading the schema (proto file) to the Schema Registry
We can do so by using the REST API exposed by the Schema Registry. Here is a handy command:
```
PROTO_CONTENT=$(cat path/to/proto/file)

curl -X POST \
	-u myApiKey:myApiSecret \
	-H "Content-Type: application/json" \
	--data '{
  	"schemaType": "PROTOBUF",
  	"schema": '"$PROTO_CONTENT"'
	}' \
	http://localhost:8081/subjects/kafka-protobuf-topic/versions
```

### Testing the app E2E
Using a gRPC client, call the Greeting endpoint by passing your `firstname` and `lastname`.
The `lastname` will be encrypted during the Protobuf Serialization.
Either use the script `./kafka-console-consumer.sh` to read the message or the my dedicated consumer app.


