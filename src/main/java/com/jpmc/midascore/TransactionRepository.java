package com.jpmc.midascore;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {
    // No custom methods needed for now
}
