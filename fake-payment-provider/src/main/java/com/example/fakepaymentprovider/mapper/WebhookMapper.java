package com.example.fakepaymentprovider.mapper;

import com.example.fake.dto.StatusUpdate;
import com.example.fakepaymentprovider.entity.WebhookPayload;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WebhookMapper {
    WebhookPayload toPayload(StatusUpdate request);
}
