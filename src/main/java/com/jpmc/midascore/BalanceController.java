package com.jpmc.midascore;


import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Optional;
import com.jpmc.midascore.foundation.Balance;


@RestController
public class BalanceController {

    private final UserRepository userRepository;

    public BalanceController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam("userId") Long userId) {
        // Try to find user by ID
        Optional<User> userOptional = userRepository.findById(userId);

        // If user not found, return balance = 0
        if (userOptional.isEmpty()) {
            return new Balance(BigDecimal.ZERO.floatValue());

        }

        // If found, return their actual balance
        BigDecimal balance = userOptional.get().getBalance();
        return new Balance(balance.floatValue());

    }
}

