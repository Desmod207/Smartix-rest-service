package com.example.data;

import com.example.entities.ApplicationUser;
import com.example.entities.Payment;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PaymentRepository extends PagingAndSortingRepository<Payment, Long> {

    List<Payment> findAllByUser(ApplicationUser user, PageRequest pageRequest);

    void save(Payment payment);

}
