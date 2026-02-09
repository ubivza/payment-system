package com.example.fakepaymentprovider.service;

import com.example.fake.dto.StatusUpdate;
import com.example.fakepaymentprovider.exception.NotValidException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;

@Slf4j
@Service
@RequiredArgsConstructor
public class HMACAuthService {
    private final HmacUtils hmacUtils;
    private final ObjectMapper objectMapper;

    public boolean authenticate(String xSignature, StatusUpdate request) {
        try {
            String encodedBody = hmacUtils.hmacHex(objectMapper.writeValueAsString(request));

            //для избежания временных атак (когда замеряется проверка байтов в equals и на основании изменения подбирается верная последовательность)
            return MessageDigest.isEqual(encodedBody.getBytes(), xSignature.getBytes());
        } catch (JsonProcessingException e) {
            throw new NotValidException("Failed to parse request");
        }
    }
}
