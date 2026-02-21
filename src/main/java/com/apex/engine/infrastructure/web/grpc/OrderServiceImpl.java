package com.apex.engine.infrastructure.web.grpc;

import com.apex.engine.application.services.OrderService;
import com.apex.engine.v1.grpc.OrderDTO;
import com.apex.engine.v1.grpc.OrderResponse;
import com.apex.engine.v1.grpc.OrderServiceGrpcGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class OrderServiceImpl extends OrderServiceGrpcGrpc.OrderServiceGrpcImplBase {

    private final OrderService orderService;

    public OrderServiceImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public void createOrder(OrderDTO request, StreamObserver<OrderResponse> responseObserver) {

        orderService.registerOrder(request);

        responseObserver.onNext(OrderResponse.newBuilder().setMessage("Order Created").setSuccess(true).build());
        responseObserver.onCompleted();
    }
}
