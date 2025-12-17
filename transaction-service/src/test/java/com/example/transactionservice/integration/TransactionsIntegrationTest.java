package com.example.transactionservice.integration;

import com.example.transaction.dto.ConfirmRequest;
import com.example.transaction.dto.CreateWalletRequest;
import com.example.transaction.dto.DepositInitRequest;
import com.example.transaction.dto.InitTransactionRequest;
import com.example.transaction.dto.TransactionConfirmResponse;
import com.example.transaction.dto.TransactionInitResponse;
import com.example.transaction.dto.TransactionStatusResponse;
import com.example.transaction.dto.TransferInitRequest;
import com.example.transactionservice.config.Container;
import com.example.transactionservice.config.KafkaTestContainer;
import com.example.transactionservice.entity.TransactionStatus;
import com.example.transactionservice.kafka.api.DepositCompletedEvent;
import com.example.transactionservice.kafka.api.DepositRequestedEvent;
import com.example.transactionservice.repository.TransactionsRepository;
import com.example.transactionservice.repository.WalletsRepository;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionsIntegrationTest extends Container {
    @Value("${kafka.topic.transaction-service.transaction.insert.name}")
    String writeTopic;
    @Value("${kafka.topic.transaction-service.transaction.read.name}")
    String readTopic;
    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    TransactionsRepository transactionsRepository;
    @Autowired
    WalletsRepository walletsRepository;
    @MockitoBean
    private JwtDecoder jwtDecoder;

    static {
        Container.start();
    }

    @AfterEach
    void clear() {
        transactionsRepository.deleteAll();
        walletsRepository.deleteAll();
    }

    @BeforeEach
    void stubSecurity() {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("scope", "message:read")
                .build();

        Mockito.when(jwtDecoder.decode(anyString())).thenReturn(jwt);
    }

    @Test
    @DisplayName("Test create wallet -> init transfer -> confirm transfer flow success")
    void testCreateWalletThanTransfer() {
        UUID userId = UUID.randomUUID();

        CreateWalletRequest createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setName("My wallet");
        createWalletRequest.setCurrencyCode("RUB");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<CreateWalletRequest> createWalletRequestHttpEntity = new HttpEntity<>(createWalletRequest, headers);

        String createWalletUrl = UriComponentsBuilder.fromPath("/v1/wallets/{userUid}")
                .uriVariables(Map.of("userUid", userId))
                .toUriString();

        ResponseEntity<UUID> createWalletResponse1 = restTemplate.exchange(createWalletUrl, HttpMethod.POST, createWalletRequestHttpEntity, UUID.class);

        assertEquals(1, walletsRepository.findAll().size());

        UUID wallet1Id = UUID.fromString(createWalletResponse1.getBody().toString());

        ResponseEntity<UUID> createWalletResponse2 = restTemplate.exchange(createWalletUrl, HttpMethod.POST, createWalletRequestHttpEntity, UUID.class);

        assertEquals(2, walletsRepository.findAll().size());

        UUID wallet2Id = UUID.fromString(createWalletResponse2.getBody().toString());

        TransferInitRequest transferInitRequest = new TransferInitRequest();
        transferInitRequest.setType("transfer");
        transferInitRequest.setUserUid(userId);
        transferInitRequest.setWalletFromUid(wallet1Id);
        transferInitRequest.setWalletToUid(wallet2Id);
        transferInitRequest.setAmount(new BigDecimal(0));

        InitTransactionRequest initTransactionRequest = transferInitRequest;

        String initTransferUrl = UriComponentsBuilder.fromPath("/v1/transactions/{type}/init")
                .uriVariables(Map.of("type", "transfer"))
                .toUriString();

        HttpEntity<InitTransactionRequest> initTransactionRequestHttpEntity = new HttpEntity<>(initTransactionRequest, headers);

        ResponseEntity<TransactionInitResponse> initTransferResponse = restTemplate.exchange(initTransferUrl, HttpMethod.POST, initTransactionRequestHttpEntity, TransactionInitResponse.class);

        assertEquals(200, initTransferResponse.getStatusCode().value());
        assertEquals(new BigDecimal(0).doubleValue(), initTransferResponse.getBody().getFee().doubleValue());
        assertNotNull(initTransferResponse.getBody().getToken());

        ConfirmRequest transferConfirmRequest = new ConfirmRequest();
        transferConfirmRequest.setConfirm(true);
        transferConfirmRequest.setToken(initTransferResponse.getBody().getToken());

        String confirmTransferUrl = UriComponentsBuilder.fromPath("/v1/transactions/{type}/confirm")
                .uriVariables(Map.of("type", "transfer"))
                .toUriString();

        HttpEntity<ConfirmRequest> transferConfirmRequestHttpEntity = new HttpEntity<>(transferConfirmRequest, headers);

        ResponseEntity<TransactionConfirmResponse> confirmTransferResponse = restTemplate.exchange(confirmTransferUrl, HttpMethod.POST, transferConfirmRequestHttpEntity, TransactionConfirmResponse.class);

        assertEquals(1, transactionsRepository.findAll().size());
        assertEquals(200, confirmTransferResponse.getStatusCode().value());
        assertEquals(TransactionStatus.COMPLETED.name(), confirmTransferResponse.getBody().getStatus());
    }

    @Test
    @DisplayName("Test create wallet -> init deposit -> check status -> receive DepositCompletedEvent flow success")
    void testCreateWalletThanInitDeposit() throws ExecutionException, InterruptedException {
        UUID userId = UUID.randomUUID();

        CreateWalletRequest createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setName("My wallet");
        createWalletRequest.setCurrencyCode("RUB");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        HttpEntity<CreateWalletRequest> createWalletRequestHttpEntity = new HttpEntity<>(createWalletRequest, headers);

        String createWalletUrl = UriComponentsBuilder.fromPath("/v1/wallets/{userUid}")
                .uriVariables(Map.of("userUid", userId))
                .toUriString();

        ResponseEntity<UUID> createWalletResponse1 = restTemplate.exchange(createWalletUrl, HttpMethod.POST, createWalletRequestHttpEntity, UUID.class);

        assertEquals(1, walletsRepository.findAll().size());

        UUID wallet1Id = UUID.fromString(createWalletResponse1.getBody().toString());

        DepositInitRequest depositInitRequest = new DepositInitRequest();
        depositInitRequest.setType("transfer");
        depositInitRequest.setUserUid(userId);
        depositInitRequest.setWalletUid(wallet1Id);
        depositInitRequest.setAmount(new BigDecimal(500));

        InitTransactionRequest initTransactionRequest = depositInitRequest;

        String initTransferUrl = UriComponentsBuilder.fromPath("/v1/transactions/{type}/init")
                .uriVariables(Map.of("type", "deposit"))
                .toUriString();

        HttpEntity<InitTransactionRequest> initTransactionRequestHttpEntity = new HttpEntity<>(initTransactionRequest, headers);

        ResponseEntity<TransactionInitResponse> initTransferResponse = restTemplate.exchange(initTransferUrl, HttpMethod.POST, initTransactionRequestHttpEntity, TransactionInitResponse.class);

        assertEquals(200, initTransferResponse.getStatusCode().value());
        assertEquals(new BigDecimal(0).doubleValue(), initTransferResponse.getBody().getFee().doubleValue());
        assertNotNull(initTransferResponse.getBody().getToken());

        ConfirmRequest transferConfirmRequest = new ConfirmRequest();
        transferConfirmRequest.setConfirm(true);
        transferConfirmRequest.setToken(initTransferResponse.getBody().getToken());

        String confirmTransferUrl = UriComponentsBuilder.fromPath("/v1/transactions/{type}/confirm")
                .uriVariables(Map.of("type", "deposit"))
                .toUriString();

        HttpEntity<ConfirmRequest> depositConfirmRequestHttpEntity = new HttpEntity<>(transferConfirmRequest, headers);

        ResponseEntity<TransactionConfirmResponse> confirmDepositResponse = restTemplate.exchange(confirmTransferUrl, HttpMethod.POST, depositConfirmRequestHttpEntity, TransactionConfirmResponse.class);

        assertEquals(1, transactionsRepository.findAll().size());
        assertEquals(200, confirmDepositResponse.getStatusCode().value());
        assertEquals(TransactionStatus.PENDING.name(), confirmDepositResponse.getBody().getStatus());

        String getStatusUrl = UriComponentsBuilder.fromPath("/v1/transactions/{transactionId}/status")
                .uriVariables(Map.of("transactionId", confirmDepositResponse.getBody().getTransactionId()))
                .toUriString();

        HttpEntity<TransactionStatusResponse> getStatusRequestHttpEntity = new HttpEntity<>(headers);

        ResponseEntity<TransactionStatusResponse> getStatusResponse = restTemplate.exchange(getStatusUrl, HttpMethod.GET, getStatusRequestHttpEntity, TransactionStatusResponse.class);

        assertEquals(200, getStatusResponse.getStatusCode().value());
        assertEquals(TransactionStatus.PENDING.name(), getStatusResponse.getBody().getStatus());

        KafkaConsumer<String, Object> consumer = KafkaTestContainer.getKafkaConsumerForTopic(writeTopic, DepositRequestedEvent.class);

        ConsumerRecords<String, Object> records = consumer.poll(Duration.ofSeconds(5));

        assertEquals(1, records.count());
        assertEquals(confirmDepositResponse.getBody().getTransactionId().toString(), records.iterator().next().key());

        KafkaProducer<String, Object> producer = KafkaTestContainer.getKafkaProducerForTopic(readTopic, DepositCompletedEvent.class);

        DepositCompletedEvent depositCompletedEvent = DepositCompletedEvent.builder()
                .transactionId(confirmDepositResponse.getBody().getTransactionId())
                .amount(new BigDecimal(500))
                .status("COMPLETED")
                .timestamp(Instant.now())
                .build();

        producer.send(new ProducerRecord<>(readTopic, confirmDepositResponse.getBody().getTransactionId().toString(), depositCompletedEvent)).get();
        producer.close();

        Thread.sleep(1000);

        assertNotNull(transactionsRepository.findById(confirmDepositResponse.getBody().getTransactionId()));
        assertEquals(TransactionStatus.COMPLETED, transactionsRepository.findAll().get(0).getStatus());
        assertEquals(new BigDecimal(500), walletsRepository.findById(wallet1Id).get().getBalance());
    }
}
