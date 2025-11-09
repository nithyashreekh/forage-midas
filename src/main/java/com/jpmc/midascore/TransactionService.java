package com.jpmc.midascore;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import com.jpmc.midascore.foundation.Transaction;

/**
 * Service responsible for validating and persisting incoming transactions.
 */
@Service
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final IncentiveService incentiveService;

    public TransactionService(UserRepository userRepository,
                              TransactionRepository transactionRepository, IncentiveService incentiveService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.incentiveService = incentiveService;
    }

    /**
     * Process a single incoming Transaction from Kafka:
     * - validate sender and recipient exist
     * - check sender balance >= amount
     * - if valid: persist a TransactionRecord and update balances atomically
     * - if invalid: do nothing
     */
    @Transactional
    public void process(Transaction incoming) {
        if (incoming == null) return;

        long senderId = incoming.getSenderId();
        long recipientId = incoming.getRecipientId();
        // convert float/double to BigDecimal safely
        BigDecimal amount = BigDecimal.valueOf(incoming.getAmount());

        // load users
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> recipientOpt = userRepository.findById(recipientId);

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            // invalid: missing user(s)
            return;
        }

        User sender = senderOpt.get();
        User recipient = recipientOpt.get();

        // check balance: sender.balance >= amount
        if (sender.getBalance().compareTo(amount) < 0) {
            // insufficient funds — do nothing
            return;
        }

        // at this point transaction is valid — apply changes
        sender.debit(amount);
        recipient.credit(amount);

        // --- NEW: fetch incentive from external Incentive API ---
        // incentiveService is injected into this class (add it to the constructor if not present)
        BigDecimal incentive = BigDecimal.ZERO;
        try {
            incentive = incentiveService.fetchIncentive(incoming);
            if (incentive == null) incentive = BigDecimal.ZERO;
        } catch (Exception ex) {
            // if anything goes wrong, treat incentive as zero (safe fallback)
            incentive = BigDecimal.ZERO;
            System.err.println("Incentive fetch failed, proceeding with 0 incentive: " + ex.getMessage());
        }

        // apply incentive only to recipient
        if (incentive.compareTo(BigDecimal.ZERO) > 0) {
            recipient.credit(incentive);
        }
        // save updated users (within the same transaction)
        userRepository.save(sender);
        userRepository.save(recipient);

        // persist the transaction record
        TransactionRecord record = new TransactionRecord(sender, recipient, amount);
        transactionRepository.save(record);
        // ✅ ADD THIS: print all user balances after every transaction
        System.out.println("------ Current User Balances ------");
        userRepository.findAll().forEach(u ->
                System.out.println(u.getName() + " → " + u.getBalance())
        );
        System.out.println("----------------------------------");
    }
}
