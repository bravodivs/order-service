package com.example.orderservice.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class OrderRequest {

    @NotNull(message = "Product list must not be provided")
    private List< @Valid ProductRequest> products;

    @NotNull(message = "Username must be provided")
    @NotEmpty(message = "Username must not be empty")
    private String username;
}
