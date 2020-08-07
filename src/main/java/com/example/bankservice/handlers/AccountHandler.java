package com.example.bankservice.handlers;

import com.example.bankservice.dto.AccountTransferFailure;
import com.example.bankservice.dto.AccountTransferRequest;
import com.example.bankservice.dto.AccountTransferResponse;
import com.example.bankservice.model.Account;
import com.example.bankservice.model.AccountType;
import com.mongodb.reactivestreams.client.ClientSession;
import com.mongodb.reactivestreams.client.MongoClient;
import jdk.jfr.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.springframework.web.reactive.function.server.ServerResponse.*;

@Slf4j
@Component
public class AccountHandler {

    @Autowired
    private ReactiveMongoTemplate template;

    @Autowired
    private MongoClient client;

    public Mono<ServerResponse> getAllAccounts(ServerRequest serverRequest) {
        var accounts = template.findAll(Account.class);
        return ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(accounts, Account.class);
    }

    @Transactional
    public Mono<ServerResponse> createAccount(ServerRequest request) {
        var account = request.bodyToMono(Account.class);
        var savedAccount = template.inTransaction().execute(reactiveMongoOperations -> reactiveMongoOperations.save(account),ClientSession::startTransaction);

        return ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(savedAccount, Account.class);
    }

    @Transactional
    public Mono<ServerResponse> transferAmount(ServerRequest request) {
        // Request
        var accountTransferRequest = request.bodyToMono(AccountTransferRequest.class);
        // From and To Account
        return accountTransferRequest.flatMap(req -> {
            var fromAccount = template.findById(req.getFromAccountId(), Account.class);
            var toAccount = template.findById(req.getToAccountId(), Account.class);
            return fromAccount
                    .flatMap(fAcc -> toAccount.flatMap(tAcc -> {
                        log.info("From Account : " + fAcc.getUser().getId());
                        log.info("To Account : " + tAcc.getUser().getId());
                        // Check if the transfer is between accounts of same user
                        if (fAcc.getUser().getId().equals(tAcc.getUser().getId())) return failureServerResponse(HttpStatus.BAD_REQUEST, "Transferring to your own account is not allowed");
                        // Check if transfer amount is available in source account
                        if (req.getAmount() >= fAcc.getAmount()) return failureServerResponse(HttpStatus.BAD_REQUEST, "Required amount not available");
                        // Check if the destination amount type is Basic Savings and the total balance do not cross 50000
                        if (tAcc.getAccountType().equals(AccountType.BasicSavings)
                                && tAcc.getAmount()+req.getAmount() > 50000) return failureServerResponse(HttpStatus.BAD_REQUEST, "Basic Savings account cannot have more than 50000");
                        // Updating Source and Destination account
                        fAcc.setAmount(fAcc.getAmount()-req.getAmount());
                        tAcc.setAmount(tAcc.getAmount()+req.getAmount());

                        var response = template.inTransaction()
                                .execute(reactiveMongoOperations -> reactiveMongoOperations.save(fAcc)
                                        .then(reactiveMongoOperations.save(tAcc).map(aa -> reactiveMongoOperations))
                                        .then(reactiveMongoOperations.find(
                                                new Query(Criteria.where("user").is(tAcc.getUser())),
                                                Account.class)
                                                .map(Account::getAmount)
                                                .reduce(0.0, (x1, x2) -> x1 + x2)
                                                .flatMap(totalDestAmount -> {
                                                    var res = AccountTransferResponse.builder()
                                                            .newSourceBalance(fAcc.getAmount())
                                                            .totalDestBalance(totalDestAmount)
                                                            .timestamp(LocalDateTime.now())
                                                            .build();
                                                    return Mono.just(res);
                                                })));
                        return ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(response, AccountTransferResponse.class);
            }));
        });
    }

    private Mono<ServerResponse> failureServerResponse(HttpStatus httpStatus, String message) {
        var failure = AccountTransferFailure.builder()
                .errorCode(httpStatus)
                .errorMessage(message)
                .build();
        return badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(failure), AccountTransferFailure.class);
    }

}
