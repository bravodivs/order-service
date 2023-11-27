package com.example.orderservice.utils;

import com.example.orderservice.exception.CustomException;
import com.example.orderservice.model.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OrderUtils {
    private static final Logger logger = LoggerFactory.getLogger(OrderUtils.class);

    private OrderUtils() {
    }

    public static void validateOrder(OrderRequest order) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        List<String> violationsList = new ArrayList<>();
        order.getProducts().forEach(productRequest -> {
            Set<ConstraintViolation<ProductRequest>> violations = validator.validate(productRequest);
            violations.forEach(violation -> violationsList.add(violation.getMessage()));
        });
        if (!violationsList.isEmpty()) {
            logger.error("violations failed - {}", violationsList);
            throw new CustomException(String.format("The following violations failed - %s", violationsList), HttpStatus.BAD_REQUEST);
        }
    }

    public static OrderResponse orderToOrderResponse(Order order){
        OrderResponse orderResponse = new OrderResponse();
        BeanUtils.copyProperties(order, orderResponse );

//        List<ProductResponse> productResponses = new ArrayList<>();
//        List<ProductRequest> productRequests = order.getProductList();
//
//        productRequests.forEach(productRequest -> {
//            ProductResponse productResponse = new ProductResponse(productRequest.getProductId(), productRequest.getProductQuantity());
//        });
//
//        orderResponse.setProductList(productResponses);

        logger.info("converted order to order response - {}", orderResponse.toString());
        return orderResponse;
    }

    public static Order orderResponseToOrder(OrderResponse orderResponse){
        Order order = new Order();
        BeanUtils.copyProperties(orderResponse, order);

//        List<ProductRequest> requestList = new ArrayList<>();
//        List<ProductResponse> responseList = orderResponse.getProductList();
//        responseList.forEach(productResponse -> {
//            ProductRequest productRequest = new ProductRequest(productResponse.getId(), productResponse.getQuantity());
//            requestList.add(productRequest);
//        });
//
//        order.setProductList(requestList);

        logger.info("converted order response to order - {}", order);
        return order;
    }

}
