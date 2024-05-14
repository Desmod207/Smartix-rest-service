package com.example.data.impl;

import com.example.data.PaymentRepository;
import com.example.entities.ApplicationUser;
import com.example.entities.Payment;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PaymentNamedParameterJdbcOperationsRepository implements PaymentRepository {

    String FIND_ALL_BY_USER_SQL = """
                SELECT * FROM payment
                WHERE user_id = :user_id
                LIMIT :limit OFFSET :offset
                """;

    String ADD_PAYMENT_TO_USER_SQL = """
                INSERT INTO payment
                    (date, phone, amount, user_id)
                VALUES (:date, :phone, :amount, :user_id)
                """;

    private final NamedParameterJdbcOperations jdbcOperations;

    public PaymentNamedParameterJdbcOperationsRepository(NamedParameterJdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    public List<Payment> findAllByUser(ApplicationUser user, PageRequest pageRequest) {
        SqlParameterSource parameterSource = new MapSqlParameterSource(
                Map.of("user_id", user.getId(),
                        "limit", pageRequest.getPageSize(),
                        "offset", pageRequest.getOffset())
        );

        return jdbcOperations.query(
                FIND_ALL_BY_USER_SQL,
                parameterSource,
                (rs, rowNum) -> new Payment(
                        rs.getLong("id"),
                        rs.getDate("date"),
                        rs.getString("phone"),
                        rs.getLong("amount"),
                        user));
    }

    @Override
    public void save(Payment payment) {
        jdbcOperations.update(
                ADD_PAYMENT_TO_USER_SQL,
                Map.of("date", payment.getDate(),
                        "phone", payment.getPhone(),
                        "amount", payment.getAmount(),
                        "user_id", payment.getUser().getId())
        );
    }

}
