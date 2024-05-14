package com.example.data;

import com.example.entities.ApplicationUser;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ApplicationUserRepository extends CrudRepository<ApplicationUser, Long> {

    Optional<ApplicationUser> findByLogin(String login);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    ApplicationUser save(ApplicationUser applicationUser);

}
