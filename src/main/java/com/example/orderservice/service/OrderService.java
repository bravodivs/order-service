package com.example.orderservice.service;

import com.example.orderservice.components.RestTemplateResponseErrorHandler;
import com.example.orderservice.exception.CustomException;
import com.example.orderservice.model.*;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.utils.OrderUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.orderservice.constants.OrderConstants.QUANTITY;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    RestTemplate restTemplate = new RestTemplateBuilder()
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .errorHandler(new RestTemplateResponseErrorHandler())
            .build();
    WebClient client = WebClient.create();

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private String accessToken;

    @Value("${spring.app.user-service-url}")
    private String userUrl;

    @Value("${spring.app.product-service-url}")
    private String productUrl;

    private static void logTraceResponse(ClientResponse response) {

        logger.error("User service error status: {}", response.statusCode());
        logger.error("User service error headers: {}", response.headers().asHttpHeaders());
        response.bodyToMono(String.class)
                .subscribe(body -> logger.error("User service error body: {}", body));
    }

    public OrderResponse placeOrder(OrderRequest orderRequest, String accessToken) {
        this.accessToken = accessToken.substring(7);

        logger.info("order request for {}", orderRequest.toString());

        var ref = new Object() {
            Double totalCost = 0.0;
        };

        OrderUtils.validateOrder(orderRequest);

        /*- use order response instead. at the end convert to order only while saving*/
        OrderResponse orderResponse = new OrderResponse();
        List<ProductResponse> productResponseList = new ArrayList<>();

        UserDto receivedUserDetails = fetchUserDetailsFromApi(orderRequest.getUsername()).block();
        logger.info("Blocked mono user and received as {}", receivedUserDetails.toString());
        orderResponse.setUser(new User(receivedUserDetails.getUsername(), receivedUserDetails.getAddress().toString(), receivedUserDetails.getMobileNumber()));

//        orderResponse.setTotalCost(checkProductExists(orderRequest.getProductRequests()));
//        order.setProductList(orderRequest.getProductRequests());
        fetchProductListFromApi(orderRequest.getProducts()).forEach(receivedProduct -> {
            ProductResponse productResponse = new ProductResponse();
            BeanUtils.copyProperties(receivedProduct, productResponse);
            ref.totalCost += receivedProduct.getPrice() * productResponse.getQuantity();
            productResponseList.add(productResponse);
            logger.info("properties copied - {}", productResponse.toString());
        });
        logger.info("Product reponse list is - {}", productResponseList);
        orderResponse.setProductList(productResponseList);
        orderResponse.setTotalCost(ref.totalCost);
        logger.info("formed order esponse is - {}", orderResponse.toString());
//        Order placedOrder;
        OrderResponse placedOrder;
        try {
            placedOrder = OrderUtils.orderToOrderResponse(orderRepository.save(OrderUtils.orderResponseToOrder(orderResponse)));
        } catch (CustomException cx) {
            throw cx;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new CustomException("error saving order", HttpStatus.BAD_REQUEST);
        }
        logger.info("order saved");

        decreaseQuantity(orderRequest.getProducts());

        return placedOrder;
    }

    /*: should giv order response with whatever populated fields*/
    private List<ReceivedProduct> fetchProductListFromApi(List<ProductRequest> productRequestList) {
        logger.info("call for checking product- {}", productRequestList.toString());

        final String showProductsUrl = productUrl + "/show_products";
        List<ReceivedProduct> receivedProductList;
        List<ReceivedProduct> finalList = new ArrayList<>();

        var ref = new Object() {
            String id = "";
        };

        List<String> notFoundProducts = new ArrayList<>();

        try {
            receivedProductList = restTemplate.exchange(
                            showProductsUrl,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<ReceivedProduct>>() {
                            })
                    .getBody();

            for (ProductRequest request : productRequestList) {
                boolean productFound = false;
                for (ReceivedProduct receivedProduct : receivedProductList) {
                    if (request.getProductId().equals(receivedProduct.getId())) {
                        productFound = true;
                        if (receivedProduct.getQuantity() < request.getProductQuantity()) {
                            logger.error("Insufficient quantity {} for {}", receivedProduct.getQuantity(), receivedProduct.getName());
                            throw new CustomException(String.format("Insufficient quantity of product %s", receivedProduct.getName()), HttpStatus.BAD_REQUEST);
                        } else {
                            logger.info("Setting quantity");
                            receivedProduct.setQuantity(request.getProductQuantity());
                            finalList.add(receivedProduct);
                        }
                        break;
                    }
                }
                if (!productFound) {
                    logger.info("product not found");
                    notFoundProducts.add(request.getProductId());
                }
            }

            if (!notFoundProducts.isEmpty()) {
                // Throw an error if any requested products are not found
                String notFoundProductsString = String.join(", ", notFoundProducts);
                throw new CustomException(String.format("Products not found: %s", notFoundProductsString), HttpStatus.NOT_FOUND);
            }

        } catch (CustomException cx) {
            logger.error(cx.getMessage());
            throw cx;
        }

//        try {
//            /* : add a route in api to accept list of ids and return details of them*/
//            receivedProductList = restTemplate.exchange(
//                            showProductsUrl,
//                            HttpMethod.GET,
//                            null,
//                            new ParameterizedTypeReference<List<ReceivedProduct>>() {
//                            })
//                    .getBody()
//                    .stream()
//                    .filter(receivedProduct -> {
//                        logger.info("in filter method with {}", receivedProduct.toString());
//                        if (productRequestList.stream()
//                                .anyMatch(request -> request.getProductId().equals(receivedProduct.getId()))) {
//                            logger.info("inside any match loop");
//                            // Check if the quantity is sufficient
//                            ProductRequest request = productRequestList.stream()
//                                    .filter(r -> r.getProductId().equals(receivedProduct.getId()))
//                                    .findFirst()
//                                    .orElse(null);
//                            logger.info("got product request - {}", request.toString());
//                            if (request != null) {
//                                logger.info(request.toString());
//                                if (receivedProduct.getQuantity() < request.getProductQuantity()) {
//                                    logger.error("Insufficient quantity {} for {}", receivedProduct.getQuantity(), receivedProduct.getName());
//                                    throw new CustomException(String.format("Insufficient quantity of product %s", receivedProduct.getName()), HttpStatus.BAD_REQUEST);
//                                } else {
//                                    logger.error("Setting quantity");
//                                    receivedProduct.setQuantity(request.getProductQuantity());
//                                }
//                            }
//                            else{
//                                logger.info("increasing not founds");
//                                ref.id += receivedProduct.getId()+" ";
//                            }
//                            return request != null;
//                        }
//
//                        return false;
//                    })
//                    .collect(Collectors.toList());


//cal cost and quant
//            while copying to product response, make sure to not copy the received quanityty.

//            productRequestList.forEach(productRequest -> {
//                try {
//                    String receivedProduct = restTemplate.getForObject(showProductsUrl + productRequest.getProductId(), String.class);
//                    receivedProductList.add(
//                            restTemplate.getForObject(showProductsUrl + productRequest.getProductId(), ReceivedProduct.class)
//                    );
//                    logger.info(receivedProduct);
//                } catch (Exception cx) {
//                    ref.idList += " " + productRequest.getProductId();
//                }
                /*
//                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode jsonNode = objectMapper.readTree(receivedProduct);
                    int q = jsonNode.get(QUANTITY).asInt();
                    double price = jsonNode.get("price").asDouble();
                    String name = jsonNode.get("name").asText();

                    if (q < productRequest.getProductQuantity())
                        throw new CustomException(String.format("Insufficient quantity of product %s", name), HttpStatus.BAD_REQUEST);
                    ref.totalCost += productRequest.getProductQuantity() * price;

                } catch (JsonProcessingException e) {
                    logger.info(e.getMessage());
                    throw new CustomException("internal exception", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                */
//            });
//        } catch (HttpClientErrorException hx) {
//            logger.error("client error - {}", hx.getMessage());
//            throw new CustomException("Client error. Try error", (HttpStatus) hx.getStatusCode());
//        } catch (HttpServerErrorException hsx) {
//            logger.error("server exception - {}", hsx.getMessage());
//            throw new CustomException("Server down. Try later", (HttpStatus) hsx.getStatusCode());
//        } catch (RestClientException rx) {
//            logger.error("rest client exception - {}", rx.getMessage());
//            throw new CustomException("Api down. Try later", HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//        return ref.totalCost;
//        if(ref.id.length()>0 || receivedProductList.isEmpty()){
//            logger.error("products {} not found", ref.id);
//            throw new CustomException(String.format("Following products were not found - %s", ref.id), HttpStatus.BAD_REQUEST);
//        }
        logger.info("returning received prod list");
        return finalList;
    }

    private void decreaseQuantity(List<ProductRequest> productRequest) {
        logger.info("call for decreasing the product quantity");
        final String showProductsUrl = productUrl + "/show_products/";
        final String updateProductUrl = productUrl + "/update/";
        productRequest.forEach(productRequest1 -> {
            try {

                String receivedProduct = restTemplate.getForObject(showProductsUrl + productRequest1.getProductId(), String.class);
                logger.info(receivedProduct);

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(receivedProduct);
                int q = jsonNode.get(QUANTITY).asInt();

                int updatedQuantity = q - productRequest1.getProductQuantity();

                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put(QUANTITY, updatedQuantity);
                HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
                restTemplate.exchange(updateProductUrl + productRequest1.getProductId(),
                        HttpMethod.PUT,
                        requestEntity,
                        String.class);

                logger.info("product updated with decreased quantity");
            } catch (JsonProcessingException e) {
                logger.info(e.getMessage());
                throw new CustomException("internal exception", HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                logger.error(e.getMessage());
                throw new CustomException("internal error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    private Mono<UserDto> fetchUserDetailsFromApi(String username) {
        logger.info("Call for user check {}", username);
        Mono<UserDto> res = null;
        try {
            client = WebClient.builder()
                    .baseUrl(userUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .build();

            res = client.get()
                    .uri("/view/{username}", username)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, response -> {
                        logger.error("Unexpected client error");
                        logTraceResponse(response);
                        if (response.statusCode().isSameCodeAs(HttpStatus.BAD_REQUEST))
                            return Mono.error(new CustomException("Bearer token not present", HttpStatus.BAD_REQUEST));
                        else
                            return Mono.error(new CustomException("User not found or disabled", HttpStatus.NOT_FOUND));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, response -> {
                        logger.error("Unexpected server error");
                        logTraceResponse(response);
                        return Mono.error(new CustomException("Server error", (HttpStatus) response.statusCode()));
                    })
                    .bodyToMono(UserDto.class)
                    .doOnTerminate(() -> logger.info("WebClient request completed"));

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new CustomException("User Service down. Check later", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return res;
    }

    public Order getOrderDetails(String orderId) {
        logger.info("Order details requested for order id - {}", orderId);
        Order order = findOrderById(orderId);
        logger.info("Found order - {}", order.toString());
        return order;
    }

    public List<Order> getAllOrders() {
        List<Order> orderList;
        orderList = orderRepository.findAll();
        if (orderList.isEmpty()) {
            logger.error("Order list is empty");
            throw new CustomException("There are no orders to be shown", HttpStatus.BAD_REQUEST);
        }
        return orderList;
    }

    public void rabbitTestRoute() {
        rabbitTemplate.convertAndSend("", "order-service", new Order());
    }

    private Order findOrderById(String id) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isEmpty()) {
            logger.error("Order with id {} not found", id);
            throw new CustomException(String.format("Order with id %s not found", id), HttpStatus.NOT_FOUND);
        }
        return order.get();
    }
}
