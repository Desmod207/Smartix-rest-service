package com.example.api.dto;

import com.example.entities.ApplicationUser;
import com.example.entities.Gender;

import java.sql.Date;

public record UserData(
        String login,
        String firstName,
        String lastName,
        String patronymic,
        Gender gender,
        String email,
        Date birthday) {

    public UserData(ApplicationUser user) {
        this(user.getLogin(), user.getFirstName(), user.getLastName(), user.getPatronymic(), user.getGender(),
                user.getEmail(), user.getBirthday());
    }

}
