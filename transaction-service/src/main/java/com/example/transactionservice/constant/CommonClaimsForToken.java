package com.example.transactionservice.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CommonClaimsForToken {
    public static final String DEPOSIT_INIT = "init-deposit";
    public static final String WITHDRAWAL_INIT = "init-withdrawal";
    public static final String TRANSFER_INIT = "init-transfer";
    public static final String ISSUER_NAME = "transaction-service";
}
