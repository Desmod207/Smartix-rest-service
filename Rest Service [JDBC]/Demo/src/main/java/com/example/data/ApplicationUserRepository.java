package com.example.data;

import com.example.entities.ApplicationUser;

import java.util.Optional;

public interface ApplicationUserRepository {

    Optional<ApplicationUser> findByLogin(String login);

    ApplicationUser save(ApplicationUser applicationUser);

    ApplicationUser update(ApplicationUser applicationUser);

}
