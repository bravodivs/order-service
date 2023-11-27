package com.example.orderservice.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    @NotNull(message = "Product id must be provided")
    @NotEmpty(message = "Product should not be empty")
    private String productId;

    @NotNull(message = "Product quantity must be provided")
    @Positive(message = "Provided product quantity must be a positive number")
    private Integer productQuantity;
}
