package com.marcofaccani.kafka.protobuf.field.encryption.channel.inbound;

import com.google.protobuf.Empty;
import com.marcofaccani.grpc.server.v1.GreetingRequest;
import com.marcofaccani.grpc.server.v1.GreetingResponse;
import com.marcofaccani.grpc.server.v1.GrpcServerServiceGrpc;
import com.marcofaccani.grpc.server.v1.ServerServiceStatus;
import com.marcofaccani.grpc.server.v1.ServerStatusReply;
import com.marcofaccani.kafka.protobuf.field.encryption.service.MyService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class GrpcServer extends GrpcServerServiceGrpc.GrpcServerServiceImplBase {

  private final MyService myService;

  @Override
  public void getServerStatus(Empty request, StreamObserver<ServerStatusReply> streamObserver) {
    ServerStatusReply reply = ServerStatusReply.newBuilder()
        .setStatus(ServerServiceStatus.UP)
        .setService("GRPC-SERVER")
        .build();
    streamObserver.onNext(reply);
    streamObserver.onCompleted();
  }

  @Override
  public void greeting(GreetingRequest request, StreamObserver<GreetingResponse> streamObserver) {
    myService.sendMessageToKafka(request);
    final var message = String.format("Hello %s %s!", request.getFirstname(), request.getLastname());
    final var reply = GreetingResponse.newBuilder()
        .setMessage(message)
        .build();
    streamObserver.onNext(reply);
    streamObserver.onCompleted();
  }

}
