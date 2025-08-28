package com.pragma.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
public class R2dbcTxConfig {
    @Bean
    public ReactiveTransactionManager reactiveTransactionManager(ConnectionFactory cf) {
        return new R2dbcTransactionManager(cf);
    }
    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager rtm) {
        return TransactionalOperator.create(rtm);
    }
}
