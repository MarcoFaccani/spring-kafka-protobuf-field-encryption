package com.marcofaccani.kafka.protobuf.field.encryption.channel.constant;

public enum AppBindingChannels {

  PROTOBUF_KAFKA_TOPIC("kafka-protobuf-topic");

  private final String value;

  AppBindingChannels(final String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }

}
