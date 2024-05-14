package com.example.data.impl;

import com.example.data.PaymentRepository;
import com.example.entities.ApplicationUser;
import com.example.entities.Payment;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PaymentJdbcOperationsRepository implements PaymentRepository {

    String FIND_ALL_BY_USER_SQL = """
                SELECT * FROM payment
                WHERE user_id = ?
                LIMIT ? OFFSET ?
                """;

    String ADD_PAYMENT_TO_USER_SQL = """
                INSERT INTO payment
                    (date, phone, amount, user_id)
                VALUES (?, ?, ?, ?)
                """;

    private final JdbcOperations jdbcOperations;

    public PaymentJdbcOperationsRepository(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public List<Payment> findAllByUser(ApplicationUser user, PageRequest pageRequest) {
        return jdbcOperations.query(
                FIND_ALL_BY_USER_SQL,
                (rs, rowNum) -> new Payment(
                        rs.getLong("id"),
                        rs.getDate("date"),
                        rs.getString("phone"),
                        rs.getLong("amount"),
                        user),
                user.getId(),
                pageRequest.getPageSize(),
                pageRequest.getOffset());
    }

    @Override
    public void save(Payment payment) {
        jdbcOperations.update(
                ADD_PAYMENT_TO_USER_SQL,
                payment.getDate(),
                payment.getPhone(),
                payment.getAmount(),
                payment.getUser().getId());
    }
}
