package com.example.data;

import com.example.entities.ApplicationUser;
import com.example.entities.Payment;

import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface PaymentRepository {

    List<Payment> findAllByUser(ApplicationUser user, PageRequest pageRequest);

    void save(Payment payment);

}
