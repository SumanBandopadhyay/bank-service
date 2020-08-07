package com.example.bankservice;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.connection.netty.NettyStreamFactoryFactory;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.SessionSynchronization;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;


@SpringBootApplication
public class BankServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankServiceApplication.class, args);
	}

	@Autowired
	private MongoClient mongoClient;

	@Bean
	public ReactiveMongoTemplate reactiveMongoTemplate() {
		ReactiveMongoTemplate reactiveMongoTemplate = new ReactiveMongoTemplate(mongoClient, "test");
		reactiveMongoTemplate.setSessionSynchronization(SessionSynchronization.ALWAYS);
		return reactiveMongoTemplate;
	}

//	@Bean
//	public MongoClient client() {
//		String uri = "mongodb://root:example@192.168.99.100:12345/test?replicaSet=rs0";
//		MongoClientSettings settings = MongoClientSettings.builder()
//				.streamFactoryFactory(new NettyStreamFactoryFactory())
//				.applyConnectionString(new ConnectionString(uri))
//				.build();
//
//		MongoClient mongoClient = MongoClients.create(settings);
//		return mongoClient;
//	}

}
