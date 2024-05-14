package com.example.data.impl;

import com.example.data.ApplicationUserRepository;
import com.example.entities.ApplicationUser;
import com.example.entities.Gender;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
public class ApplicationUserJdbcTemplateRepository implements ApplicationUserRepository, RowMapper<ApplicationUser> {

    String FIND_USER_BY_LOGIN_SQL = """
                SELECT  id, login, password, balance, first_name, last_name, patronymic, email, gender, birthday
                FROM application_user
                WHERE login = ?
                """;

    String SAVE_NEW_USER_SQL = """
                INSERT INTO application_user
                    (login, password, balance, first_name, last_name, patronymic, email, gender, birthday)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

    String UPDATE_NEW_USER_SQL = """
                UPDATE application_user
                SET balance = ?, first_name = ?, last_name = ?, patronymic = ?, email = ?, gender = ?, birthday = ?
                WHERE id = ?
                """;

    private final JdbcTemplate jdbcTemplate;

    public ApplicationUserJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ApplicationUser> findByLogin(String login) {
        List<ApplicationUser> results = jdbcTemplate.query(FIND_USER_BY_LOGIN_SQL, this, login);
        return results.size() == 0 ?
                Optional.empty() :
                Optional.of(results.get(0));
    }

    @Override
    public ApplicationUser save(ApplicationUser applicationUser) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(SAVE_NEW_USER_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, applicationUser.getLogin());
            ps.setString(2, applicationUser.getPassword());
            ps.setLong(3, applicationUser.getBalance());
            ps.setString(4, applicationUser.getFirstName());
            ps.setString(5, applicationUser.getLastName());
            ps.setString(6, applicationUser.getPatronymic());
            ps.setString(7, applicationUser.getEmail());
            ps.setObject(8, applicationUser.getGender(), Types.OTHER);
            ps.setDate(9, applicationUser.getBirthday());
            return ps;
        }, keyHolder);

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
        Object[] params = new Object[] {
                applicationUser.getBalance(),
                applicationUser.getFirstName(),
                applicationUser.getLastName(),
                applicationUser.getPatronymic(),
                applicationUser.getEmail(),
                applicationUser.getGender(),
                applicationUser.getBirthday(),
                applicationUser.getId()
        };
        int [] types = new int[] {
                Types.BIGINT,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.VARCHAR,
                Types.OTHER,
                Types.DATE,
                Types.BIGINT
        };
        jdbcTemplate.update(UPDATE_NEW_USER_SQL, params, types);

        return  applicationUser;
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
