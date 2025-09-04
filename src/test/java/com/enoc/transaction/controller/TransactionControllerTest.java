package com.enoc.transaction.controller;

import com.enoc.transaction.service.TransactionService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.openapitools.model.TransactionRequest;
import org.openapitools.model.TransactionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@WebFluxTest(TransactionController.class)
public class TransactionControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TransactionService transactionService;

    @Test
    void createShouldReturn201() {
        TransactionRequest request = new TransactionRequest().amount(BigDecimal.valueOf(100));
        TransactionResponse response = new TransactionResponse().id("tx123").amount(BigDecimal.valueOf(100));

        when(transactionService.create(any())).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/transactions")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(TransactionResponse.class)
                .isEqualTo(response);
    }

    @Test
    void getByIdShouldReturnTransaction() {
        TransactionResponse response = new TransactionResponse().id("tx123").amount(BigDecimal.valueOf(100));

        when(transactionService.findById("tx123")).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/transactions/tx123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionResponse.class)
                .isEqualTo(response);
    }

    @Test
    void getAllShouldReturnFlux() {
        TransactionResponse tx1 = new TransactionResponse().id("1").amount(BigDecimal.valueOf(100));
        TransactionResponse tx2 = new TransactionResponse().id("2").amount(BigDecimal.valueOf(200));

        when(transactionService.findAll()).thenReturn(Flux.just(tx1, tx2));

        webTestClient.get()
                .uri("/api/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TransactionResponse.class)
                .hasSize(2)
                .contains(tx1, tx2);
    }

    @Test
    void updateShouldReturnUpdatedTransaction() {
        TransactionRequest request = new TransactionRequest().amount(BigDecimal.valueOf(150));
        TransactionResponse response = new TransactionResponse().id("tx123").amount(BigDecimal.valueOf(150));

        when(transactionService.update(eq("tx123"), any())).thenReturn(Mono.just(response));

        webTestClient.put()
                .uri("/api/transactions/tx123")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TransactionResponse.class)
                .isEqualTo(response);
    }

    @Test
    void deleteShouldReturnNoContent() {
        when(transactionService.delete("tx123")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/transactions/tx123")
                .exchange()
                .expectStatus().isNoContent();
    }
}


