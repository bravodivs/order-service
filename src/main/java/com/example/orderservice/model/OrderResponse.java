package com.example.orderservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
public class OrderResponse {

//    @JsonProperty("Order ID")
    private String orderId;

    private List<ProductResponse> productList;

    private Double totalCost;

    private User user;

    private Date orderPlaceTime;
}
