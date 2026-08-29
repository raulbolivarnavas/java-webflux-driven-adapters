package com.raulbolivar.servicename.driven.spexecutor.adapter;

import com.raulbolivar.servicename.ports.out.StoredProcedureDecoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class Base64StoredProcedureDecoder implements StoredProcedureDecoder {

    @Override
    public String decode(String encodedSql) {
        if (encodedSql == null || encodedSql.isBlank()) {
            throw new IllegalArgumentException("Encoded SQL must not be null or blank");
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encodedSql);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Stored procedure SQL is not valid Base64", ex);
        }
    }
}
