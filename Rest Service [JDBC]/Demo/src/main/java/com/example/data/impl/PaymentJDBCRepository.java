package com.example.data.impl;

import com.example.data.PaymentRepository;
import com.example.entities.ApplicationUser;
import com.example.entities.Payment;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@Primary
public class PaymentJDBCRepository implements PaymentRepository {

    private final DataSource dataSource;

    public PaymentJDBCRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Payment> findAllByUser(ApplicationUser user, PageRequest pageRequest) {
        String sql = """
                SELECT * FROM payment
                WHERE user_id = ?
                LIMIT ? OFFSET ?
                """;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, user.getId());
            statement.setLong(2, pageRequest.getPageSize());
            statement.setLong(3, pageRequest.getOffset());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Payment> payments = new ArrayList<>();
                while (resultSet.next()) {
                    Payment payment = new Payment(
                            resultSet.getLong("id"),
                            resultSet.getTimestamp("date"),
                            resultSet.getString("phone"),
                            resultSet.getLong("amount"),
                            user
                    );
                    payments.add(payment);
                }
                return payments;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public void save(Payment payment) {
        String sql = """
                INSERT INTO payment
                    (date, phone, amount, user_id)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            statement.setString(2, payment.getPhone());
            statement.setLong(3, payment.getAmount());
            statement.setLong(4, payment.getUser().getId());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
