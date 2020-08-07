package com.example.bankservice.resource;

import com.example.bankservice.handlers.AccountHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
public class AccountResource {

    @Bean
    public RouterFunction<ServerResponse> accountRoutes(AccountHandler accountHandler) {
        return route(GET("/accounts"), accountHandler::getAllAccounts)
                .andRoute(POST("/account"), accountHandler::createAccount)
                .andRoute(POST("/transfer"), accountHandler::transferAmount);
    }

}
