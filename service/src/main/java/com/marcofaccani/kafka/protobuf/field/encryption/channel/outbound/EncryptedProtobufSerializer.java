package com.marcofaccani.kafka.protobuf.field.encryption.channel.outbound;


import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.marcofaccani.kafka.protobuf.field.encryption.util.EncryptionService;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EncryptedProtobufSerializer<T extends Message> extends KafkaProtobufSerializer<T> {

  private final EncryptionService encryptionService = new EncryptionService();

  @Override
  public byte[] serialize(String topic, T data) {
    T encryptedData = encryptFields(data);
    return super.serialize(topic, encryptedData);
  }

  private T encryptFields(T message) {
    Message.Builder builder = message.toBuilder();
    for (FieldDescriptor field : message.getDescriptorForType().getFields()) {
      if (field.getOptions().getExtension(com.marcofaccani.grpc.server.v1.MyGrpcServer.encrypt)) {
        String originalValue = (String) message.getField(field);
        String encryptedValue = encryptionService.encrypt(originalValue);
        builder.setField(field, encryptedValue);
      }
    }
    return (T) builder.build();
  }
}
