package cn.haitaoss;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class GreetingServer {
    
    public static void main(String[] args) throws Exception {
        Server server = ServerBuilder.forPort(9090).addService(new GreetingServiceImpl()).build();
        
        server.start();
        System.out.println("Server started");
        
        server.awaitTermination();
    }
    
    static class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {
        
        @Override
        public void greet(Greet.GreetRequest request, StreamObserver<Greet.GreetResponse> responseObserver) {
            String name = request.getName();
            String greeting = "Hello, " + name + "!";
            
            Greet.GreetResponse response = Greet.GreetResponse.newBuilder().setMessage(greeting).build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}