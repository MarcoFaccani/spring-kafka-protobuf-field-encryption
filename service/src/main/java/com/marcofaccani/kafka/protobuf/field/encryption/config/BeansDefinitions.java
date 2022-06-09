package com.marcofaccani.kafka.protobuf.field.encryption.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansDefinitions {

  /*
    Required to avoid cyclic-reference of protobuf messages
   */
  @Bean
  public ObjectMapper objectMapper() {
    var mapper = new ObjectMapper();
    mapper.registerModule(new ProtobufModule());
    return mapper;
  }

}
