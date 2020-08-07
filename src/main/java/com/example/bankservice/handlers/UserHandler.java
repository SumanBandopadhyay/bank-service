package com.example.bankservice.handlers;

import com.example.bankservice.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class UserHandler {

    @Autowired
    private ReactiveMongoTemplate template;

    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(template.findAll(User.class), User.class);
    }

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(template.save(request.bodyToMono(User.class)), User.class);
    }

}
