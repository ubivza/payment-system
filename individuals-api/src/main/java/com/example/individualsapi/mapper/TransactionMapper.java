package com.example.individualsapi.mapper;

import com.example.individuals.dto.ConfirmRequestDto;
import com.example.individuals.dto.DepositInitRequestDto;
import com.example.individuals.dto.TransactionConfirmResponseDto;
import com.example.individuals.dto.TransactionInitResponseDto;
import com.example.individuals.dto.TransactionStatusResponseDto;
import com.example.individuals.dto.TransferInitRequestDto;
import com.example.individuals.dto.WithdrawalInitRequestDto;
import com.example.transaction.dto.ConfirmRequest;
import com.example.transaction.dto.DepositInitRequest;
import com.example.transaction.dto.TransactionConfirmResponse;
import com.example.transaction.dto.TransactionInitResponse;
import com.example.transaction.dto.TransactionStatusResponse;
import com.example.transaction.dto.TransferInitRequest;
import com.example.transaction.dto.WithdrawalInitRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.math.BigDecimal;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {
    ConfirmRequest mapConfirm(ConfirmRequestDto externalDto);
    TransactionConfirmResponseDto mapConfirmRequest(TransactionConfirmResponse internalDto);
    TransactionStatusResponseDto mapStatusRequest(TransactionStatusResponse internalDto);
    @Mapping(target = "userUid", source = "userId")
    WithdrawalInitRequest mapWithdrawal(WithdrawalInitRequestDto initTransactionRequest, String userId);
    @Mapping(target = "userUid", source = "userId")
    DepositInitRequest mapDeposit(DepositInitRequestDto initTransactionRequest, String userId);
    @Mapping(target = "userUid", source = "userId")
    TransferInitRequest mapTransfer(TransferInitRequestDto initTransactionRequest, String userId, BigDecimal rate);
    TransactionInitResponseDto mapInitResponse(TransactionInitResponse transactionInitResponse);
}
