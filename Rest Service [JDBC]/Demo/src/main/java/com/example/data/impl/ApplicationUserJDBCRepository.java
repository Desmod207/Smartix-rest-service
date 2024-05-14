package com.example.data.impl;

import com.example.data.ApplicationUserRepository;
import com.example.entities.ApplicationUser;
import com.example.entities.Gender;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

@Repository
@Primary
public class ApplicationUserJDBCRepository implements ApplicationUserRepository {

    private final DataSource dataSource;

    public ApplicationUserJDBCRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<ApplicationUser> findByLogin(String login) {
        String sql = """
                SELECT  id, login, password, balance, first_name, last_name, patronymic, email, gender, birthday
                FROM application_user
                WHERE login = ?
                """;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, login);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    ApplicationUser user = new ApplicationUser();
                    user.setId(resultSet.getLong("id"));
                    user.setLogin(resultSet.getString("login"));
                    user.setPassword(resultSet.getString("password"));
                    user.setBalance(resultSet.getLong("balance"));
                    user.setFirstName(resultSet.getString("first_name"));
                    user.setLastName(resultSet.getString("last_name"));
                    user.setPatronymic(resultSet.getString("patronymic"));
                    user.setEmail(resultSet.getString("email"));
                    String genderValue = resultSet.getString("gender");
                    if (genderValue != null)
                        user.setGender(Gender.valueOf(genderValue));
                    user.setBirthday(resultSet.getDate("birthday"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public ApplicationUser save(ApplicationUser applicationUser) {
        String sql = """
                INSERT INTO application_user
                    (login, password, balance, first_name, last_name, patronymic, email, gender, birthday)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, applicationUser.getLogin());
            statement.setString(2, applicationUser.getPassword());
            statement.setLong(3, applicationUser.getBalance());
            statement.setString(4, applicationUser.getFirstName());
            statement.setString(5, applicationUser.getLastName());
            statement.setString(6, applicationUser.getPatronymic());
            statement.setString(7, applicationUser.getEmail());
            statement.setObject(8, applicationUser.getGender(), Types.OTHER);
            statement.setDate(9, applicationUser.getBirthday());
            statement.execute();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    applicationUser.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applicationUser;
    }

    @Override
    public ApplicationUser update(ApplicationUser applicationUser) {
        String sql = """
                UPDATE application_user
                SET balance = ?, first_name = ?, last_name = ?, patronymic = ?, email = ?, gender = ?, birthday = ?
                WHERE id = ?
                """;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, applicationUser.getBalance());
            statement.setString(2, applicationUser.getFirstName());
            statement.setString(3, applicationUser.getLastName());
            statement.setString(4, applicationUser.getPatronymic());
            statement.setString(5, applicationUser.getEmail());
            statement.setObject(6, applicationUser.getGender(), Types.OTHER);
            statement.setDate(7, applicationUser.getBirthday());
            statement.setLong(8, applicationUser.getId());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applicationUser;
    }

}
