package com.example.api.dto;

import com.example.entities.Payment;

import java.util.Date;

public record PaymentData(Date date, String phone, double amount) {

    public PaymentData(Payment payment) {
        this(payment.getDate(), payment.getPhone(), (double)payment.getAmount() / 100);
    }

}
