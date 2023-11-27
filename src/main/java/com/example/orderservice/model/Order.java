package com.example.orderservice.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Document(collection = "orders")
@ToString
public class Order {

    @Id
    private String orderId;

    private List<ProductResponse> productList;

    private User user;

    private Double totalCost;

    @CreatedDate
    private Date orderPlaceTime;

}
