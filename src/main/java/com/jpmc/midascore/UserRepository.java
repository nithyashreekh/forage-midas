package com.jpmc.midascore;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Custom method to find a user by name
    Optional<User> findByName(String name);
}
