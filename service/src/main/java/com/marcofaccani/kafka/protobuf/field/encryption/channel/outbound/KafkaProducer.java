package com.marcofaccani.kafka.protobuf.field.encryption.channel.outbound;

import com.marcofaccani.grpc.server.v1.GreetingRequest;
import com.marcofaccani.kafka.protobuf.field.encryption.channel.constant.AppBindingChannels;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

@Service
@RequiredArgsConstructor
@Log4j2
public class KafkaProducer {

  private final StreamBridge streamBridge;

  @SneakyThrows
  public void sendMessage(GreetingRequest grpcRequest) {
    final var topic = AppBindingChannels.PROTOBUF_KAFKA_TOPIC.getValue();
    streamBridge.send(topic, grpcRequest, MimeType.valueOf("application/x-protobuf"));
  }

}
