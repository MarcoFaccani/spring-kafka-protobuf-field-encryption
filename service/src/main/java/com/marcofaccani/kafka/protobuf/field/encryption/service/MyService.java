package com.marcofaccani.kafka.protobuf.field.encryption.service;

import com.marcofaccani.grpc.server.v1.GreetingRequest;
import com.marcofaccani.kafka.protobuf.field.encryption.channel.outbound.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyService {

  private final KafkaProducer kafkaProducer;

  public void sendMessageToKafka(GreetingRequest request) {
    kafkaProducer.sendMessage(request);
  }

}
