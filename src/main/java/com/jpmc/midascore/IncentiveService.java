package com.jpmc.midascore;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import com.jpmc.midascore.foundation.Transaction;

import java.math.BigDecimal;

/**
 * Service to communicate with the external Incentive API.
 * It sends a Transaction and receives an Incentive amount.
 */
@Service
public class IncentiveService {

    private static final String INCENTIVE_API_URL = "http://localhost:8080/incentive";

    private final RestTemplate restTemplate = new RestTemplate();

    public BigDecimal fetchIncentive(Transaction transaction) {
        try {
            // Send the transaction to the Incentive API and expect an Incentive response
            ResponseEntity<IncentiveResponse> response =
                    restTemplate.postForEntity(INCENTIVE_API_URL, transaction, IncentiveResponse.class);

            // Extract and return the amount
            IncentiveResponse body = response.getBody();
            if (body != null && body.getAmount() != null) {
                return body.getAmount();
            } else {
                return BigDecimal.ZERO; // default if no response
            }
        } catch (Exception e) {
            System.err.println("Error calling Incentive API: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    // Inner class to match Incentive API response
    public static class IncentiveResponse {
        private BigDecimal amount;

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}

