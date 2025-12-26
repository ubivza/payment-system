package com.example.transactionservice.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FailureReason {
    public static String NOT_ENOUGH_BALANCE = "Your wallet %s dont have enough balance to perform this operation!";
    public static String EXTERNAL_SYSTEM_FAILED = "External system not responding, try again later!";
}
