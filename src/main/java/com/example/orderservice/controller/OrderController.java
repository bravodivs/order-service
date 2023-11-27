package com.example.orderservice.controller;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping(value = "/place_order", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> placeOrder(@Valid @RequestBody OrderRequest orderRequest,
                                              @RequestHeader("Authorization") String accessToken){
        return new ResponseEntity<>(orderService.placeOrder(orderRequest, accessToken), HttpStatus.CREATED);
    }

    @GetMapping(value = "/get_orders")
    public ResponseEntity<List<Order>> getOrders(){
        return new ResponseEntity<>(orderService.getAllOrders(), HttpStatus.OK);
    }

    @GetMapping(value = "/get_order/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable String id){
        return new ResponseEntity<>(orderService.getOrderDetails(id), HttpStatus.OK);
    }
}
