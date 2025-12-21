package com.example.transactionservice.sharding;

import com.example.transactionservice.config.Container;
import com.example.transactionservice.entity.ActivityStatus;
import com.example.transactionservice.entity.WalletTypes;
import com.example.transactionservice.entity.Wallets;
import com.example.transactionservice.repository.WalletsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShardingTest extends Container {

    @Autowired
    WalletsRepository walletsRepository;

    static {
        Container.startDatabases();
    }

    @Test
    @DisplayName("Test if sharding working")
    void testSharding() {
        WalletTypes walletType1 = new WalletTypes();
        walletType1.setName("Standard Wallet Type");
        walletType1.setCurrencyCode("USD");
        walletType1.setStatus(ActivityStatus.ACTIVE);
        walletType1.setUserType("STANDARD");
        walletType1.setCreator(UUID.fromString("00000000-0000-0000-0000-000000000000").toString());
        walletType1.setModifier("modifier1");

        Wallets wallet1 = new Wallets();
        wallet1.setName("First Test Wallet");
        wallet1.setWalletType(walletType1);
        wallet1.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        wallet1.setStatus(ActivityStatus.ACTIVE);
        wallet1.setBalance(BigDecimal.valueOf(1500));


        WalletTypes walletType2 = new WalletTypes();
        walletType2.setName("Premium Wallet Type");
        walletType2.setCurrencyCode("EUR");
        walletType2.setStatus(ActivityStatus.ACTIVE);
        walletType2.setUserType("PREMIUM");
        walletType2.setCreator(UUID.fromString("00000000-0000-0000-0000-000000000001").toString());
        walletType2.setModifier("modifier2");

        Wallets wallet2 = new Wallets();
        wallet2.setName("Second Test Wallet");
        wallet2.setWalletType(walletType2);
        wallet2.setUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        wallet2.setStatus(ActivityStatus.ACTIVE);
        wallet2.setBalance(BigDecimal.valueOf(3000));

        walletsRepository.saveAll(List.of(wallet1, wallet2));

        assertEquals(2, walletsRepository.findAll().size());

        try (Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:65431/test", "admin", "admin")) {
            String select = "select * from transaction_service.wallets";
            PreparedStatement pstm = con.prepareStatement(select);
            ResultSet resultSet = pstm.executeQuery();

            int count = 0;

            while (resultSet.next()) {
                count++;
            }

            assertEquals(1, count);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:65432/test", "admin", "admin")) {
            String select = "select * from transaction_service.wallets";
            PreparedStatement pstm = con.prepareStatement(select);
            ResultSet resultSet = pstm.executeQuery();

            int count = 0;

            while (resultSet.next()) {
                count++;
            }

            assertEquals(1, count);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
