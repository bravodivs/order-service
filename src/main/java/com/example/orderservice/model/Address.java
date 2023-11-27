package com.example.orderservice.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class Address {

    private UUID id;

    private Integer pincode;

    private String city;

    private String state;

    private String addressLine1;

    private String addressLine2;

    private String country;

    @Override
    public String toString() {
        return "Addresses{" +
                "Id=" + id +
                ", pincode=" + pincode +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", addressLine1='" + addressLine1 + '\'' +
                ", addressLine2='" + addressLine2 + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
