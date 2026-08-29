package com.raulbolivar.servicename.ports.out;

public interface StoredProcedureDecoder {
    String decode(String encodedSql);
}
