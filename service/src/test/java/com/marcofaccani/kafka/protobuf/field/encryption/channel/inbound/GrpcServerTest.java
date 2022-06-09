package com.marcofaccani.kafka.protobuf.field.encryption.channel.inbound;

import com.google.protobuf.Empty;
import com.marcofaccani.grpc.server.v1.GreetingRequest;
import com.marcofaccani.grpc.server.v1.GreetingResponse;
import com.marcofaccani.grpc.server.v1.ServerServiceStatus;
import com.marcofaccani.grpc.server.v1.ServerStatusReply;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GrpcServerTest {

  @InjectMocks
  GrpcServer underTest;

  @Nested
  class GetServerStatus {

    Empty grpcRequest;

    ServerStatusReply grpcResponse;

    @Mock
    StreamObserver<ServerStatusReply> streamObserver;

    @Test
    void happyPath() {
      grpcRequest = Empty.getDefaultInstance();
      grpcResponse = ServerStatusReply.newBuilder()
          .setStatus(ServerServiceStatus.UP)
          .setService("GRPC-SERVER")
          .setNewField("A new field!")
          .build();

      assertDoesNotThrow(() -> underTest.getServerStatus(grpcRequest, streamObserver));
      verify(streamObserver).onNext(grpcResponse);
      verify(streamObserver).onCompleted();
    }
  }

  @Nested
  class Greeting {

    GreetingRequest grpcRequest;
    GreetingResponse grpcResponse;

    @Mock
    StreamObserver<GreetingResponse> streamObserver;

    @Test
    void greetMarco() {
      grpcRequest = GreetingRequest.newBuilder().setFirstname("Marco").setLastname("Rossi").build();
      assertDoesNotThrow(() -> underTest.greeting(grpcRequest, streamObserver));

      grpcResponse = GreetingResponse.newBuilder().setMessage("Hello Marco Rossi!").build();
      verify(streamObserver).onNext(grpcResponse);
      verify(streamObserver).onCompleted();
    }

    @Test
    void greetLuca() {
      grpcRequest = GreetingRequest.newBuilder().setFirstname("Luca").setLastname("Rossi").build();
      assertDoesNotThrow(() -> underTest.greeting(grpcRequest, streamObserver));

      grpcResponse = GreetingResponse.newBuilder().setMessage("Hello Luca Rossi!").build();
      verify(streamObserver).onNext(grpcResponse);
      verify(streamObserver).onCompleted();
    }
  }
}


