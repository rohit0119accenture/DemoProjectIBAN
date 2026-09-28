package com.demobank.transfer.model;

public record TransferRequest(
        String recipientName,
        String iban,
        String amount,
        String purpose
) {}
