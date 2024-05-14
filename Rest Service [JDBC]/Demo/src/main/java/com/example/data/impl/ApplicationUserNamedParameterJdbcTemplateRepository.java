package com.example.data.impl;

import com.example.data.ApplicationUserRepository;
import com.example.entities.ApplicationUser;
import com.example.entities.Gender;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class ApplicationUserNamedParameterJdbcTemplateRepository implements ApplicationUserRepository, RowMapper<ApplicationUser> {

    String FIND_USER_BY_LOGIN_SQL = """
                SELECT  id, login, password, balance, first_name, last_name, patronymic, email, gender, birthday
                FROM application_user
                WHERE login = :login
                """;

    String SAVE_NEW_USER_SQL = """
                INSERT INTO application_user
                    (login, password, balance, first_name, last_name, patronymic, email, gender, birthday)
                VALUES (:login, :password, :balance, :first_name, :last_name, :patronymic, :email, :gender, :birthday)
                """;

    String UPDATE_NEW_USER_SQL = """
                UPDATE application_user
                SET balance = :balance, first_name = :first_name, last_name = :last_name,
                    patronymic = :patronymic, email = :email, gender = :gender, birthday = :birthday
                WHERE id = :id
                """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ApplicationUserNamedParameterJdbcTemplateRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ApplicationUser> findByLogin(String login) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("login", login);

        List<ApplicationUser> results = jdbcTemplate.query(FIND_USER_BY_LOGIN_SQL, namedParameters, this);

        return results.size() == 0 ?
                Optional.empty() :
                Optional.of(results.get(0));
    }

    @Override
    public ApplicationUser save(ApplicationUser applicationUser) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("login", applicationUser.getLogin());
        namedParameters.addValue("password", applicationUser.getPassword());
        namedParameters.addValue("balance", applicationUser.getBalance());
        namedParameters.addValue("first_name", applicationUser.getFirstName());
        namedParameters.addValue("last_name", applicationUser.getLastName());
        namedParameters.addValue("patronymic", applicationUser.getPatronymic());
        namedParameters.addValue("email", applicationUser.getEmail());
        namedParameters.addValue("gender", applicationUser.getGender(), Types.OTHER);
        namedParameters.addValue("birthday", applicationUser.getBirthday());

        jdbcTemplate.update(SAVE_NEW_USER_SQL, namedParameters, keyHolder);

        long newId;
        if (keyHolder.getKeys().size() > 1) {
            newId = ((Integer)keyHolder.getKeys().get("id")).longValue();
        } else {
            newId = keyHolder.getKey().longValue();
        }
        applicationUser.setId(newId);

        return applicationUser;
    }

    @Override
    public ApplicationUser update(ApplicationUser applicationUser) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        namedParameters.addValue("id", applicationUser.getId());
        namedParameters.addValue("balance", applicationUser.getBalance());
        namedParameters.addValue("first_name", applicationUser.getFirstName());
        namedParameters.addValue("last_name", applicationUser.getLastName());
        namedParameters.addValue("patronymic", applicationUser.getPatronymic());
        namedParameters.addValue("email", applicationUser.getEmail());
        namedParameters.addValue("gender", applicationUser.getGender(), Types.OTHER);
        namedParameters.addValue("birthday", applicationUser.getBirthday());

        jdbcTemplate.update(UPDATE_NEW_USER_SQL, namedParameters);

        return applicationUser;
    }

    @Override
    public ApplicationUser mapRow(ResultSet rs, int rowNum) throws SQLException {
        var applicationUser = new ApplicationUser();

        applicationUser.setId(rs.getLong("id"));
        applicationUser.setLogin(rs.getString("login"));
        applicationUser.setPassword(rs.getString("password"));
        applicationUser.setBalance(rs.getLong("balance"));
        applicationUser.setFirstName(rs.getString("first_name"));
        applicationUser.setLastName(rs.getString("last_name"));
        applicationUser.setPatronymic(rs.getString("patronymic"));
        applicationUser.setEmail(rs.getString("email"));
        String genderValue = rs.getString("gender");
        if (genderValue != null)
            applicationUser.setGender(Gender.valueOf(genderValue));
        applicationUser.setBirthday(rs.getDate("birthday"));

        return applicationUser;
    }

}
