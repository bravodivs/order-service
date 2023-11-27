package com.example.orderservice.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ReceivedProduct {
    private String id;

    private String name;

    private String description;

    private Integer quantity;

    private Double price;

    private List<String> images;

    private Date createdAt;

    private Date modifiedAt;
}
