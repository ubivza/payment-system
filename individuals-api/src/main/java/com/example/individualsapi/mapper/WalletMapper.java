package com.example.individualsapi.mapper;

import com.example.individuals.dto.CreateWalletRequestDto;
import com.example.individuals.dto.WalletResponseDto;
import com.example.transaction.dto.CreateWalletRequest;
import com.example.transaction.dto.WalletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WalletMapper {
    WalletResponseDto mapGetResponse(WalletResponse inner);
    CreateWalletRequest mapCreate(CreateWalletRequestDto outer);
}
