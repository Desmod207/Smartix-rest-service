package com.example.api.dto;

import com.example.entities.ApplicationUser;

public record Balance(String login, double balance) {

    public Balance(ApplicationUser applicationUser) {
        this(applicationUser.getLogin(), (double)applicationUser.getBalance() / 100);
    }

}
