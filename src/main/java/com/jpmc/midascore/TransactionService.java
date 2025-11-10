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

    public TransactionService(UserRepository userRepository,
                              TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
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
