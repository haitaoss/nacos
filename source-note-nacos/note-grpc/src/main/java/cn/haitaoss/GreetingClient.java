package cn.haitaoss;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GreetingClient {
  public static void main(String[] args) {
    ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
        .usePlaintext()
        .build();
    
    GreetingServiceGrpc.GreetingServiceBlockingStub stub = GreetingServiceGrpc.newBlockingStub(channel);
    
    Greet.GreetRequest request = Greet.GreetRequest.newBuilder()
        .setName("haitaoss")
        .build();
    
    Greet.GreetResponse response = stub.greet(request);

    System.out.println("Server response: " + response.getMessage());

    channel.shutdown();
  }
}