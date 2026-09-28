package com.demobank.transfer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void submitTransfer_allFields_returnsSuccess() throws Exception {
        String json = """
                {
                    "recipientName": "Max Mustermann",
                    "iban": "DE89370400440532013000",
                    "amount": "100.50",
                    "purpose": "Invoice 12345"
                }
                """;

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Transfer to Max Mustermann submitted successfully."));
    }

    @Test
    void submitTransfer_purposeNull_returnsSuccess() throws Exception {
        String json = """
                {
                    "recipientName": "Erika Musterfrau",
                    "iban": "DE27100777770209299700",
                    "amount": "250.00",
                    "purpose": null
                }
                """;

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Transfer to Erika Musterfrau submitted successfully."));
    }
}
