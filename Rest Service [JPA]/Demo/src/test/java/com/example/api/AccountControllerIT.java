package com.example.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Sql("/sql/account_controller/test_data.sql")
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@Transactional
public class AccountControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void handleRegistrationNewUser_ValidLoginAndPassword_ReturnsValidResponse() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/api/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "login":"9876543210",
                            "password":"password1"
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isCreated(),
                        header().exists(HttpHeaders.LOCATION),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                    "login": "9876543210",
                                    "firstName": null,
                                    "lastName": null,
                                    "patronymic": null,
                                    "gender": null,
                                    "email": null,
                                    "birthday": null
                                }
                                """)
                );
    }

    @Test
    public void handleRegistrationNewUser_LoginIsNull_ReturnsValidResponse() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/api/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "password":"1"
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        header().doesNotExist(HttpHeaders.LOCATION),
                        content().string("Login and password required")
                );
    }

    @Test
    public void handleRegistrationNewUser_LoginIsEmpty_ReturnsValidResponse() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/api/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "login":"",
                            "password":"1"
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        header().doesNotExist(HttpHeaders.LOCATION),
                        content().string("Login and password required")
                );
    }

    @Test
    public void handleRegistrationNewUser_LoginNotNumber_ReturnsValidResponse() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/api/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "login":"12313asd123sass",
                            "password":"1"
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        header().doesNotExist(HttpHeaders.LOCATION),
                        content().string("Login must be phone number")
                );
    }

    @Test
    public void handleRegistrationNewUser_UserAlreadyExist_ReturnsValidResponse() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/api/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "login":"+7987654321",
                            "password":"1"
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        header().doesNotExist(HttpHeaders.LOCATION),
                        content().string("User with this login already exists")
                );
    }

    @Test
    public void handleBalance_ReturnsValidResponse() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/api/balance")
                .with(httpBasic("+7987654321", "password1"));

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                    "login":"+7987654321",
                                    "balance":1000.0
                                }
                                """)
                );
    }

    @Test
    public void handleMakingPayment_ReturnsValidResponse() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/api/payment")
                .with(httpBasic("+7987654321", "password1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "phone": "+79876543210",
                            "amount": 150
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk()
                );
    }

    @Test
    public void handleMakingPayment_PhoneIsNotNumber_ReturnsValidResponse() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/api/payment")
                .with(httpBasic("+7987654321", "password1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "phone": "+79876asdasd",
                            "amount": 150
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().string("Phone number must contain only numbers")
                );
    }

    @Test
    public void handleMakingPayment_NegativeAmount_ReturnsValidResponse() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/api/payment")
                .with(httpBasic("+7987654321", "password1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "phone": "+7987654321",
                            "amount": -150
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isBadRequest(),
                        content().string("Amount must be positive")
                );
    }

    @Test
    public void handleHistory_ReturnsValidResponse() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.get("/api/history")
                .with(httpBasic("+7987654321", "password1"));

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                [
                                    {
                                        "date": "2024-03-17T08:43:35.026+00:00",
                                        "phone": "+79876543210",
                                        "amount": 150.0
                                    }
                                ]
                                """)
                );
    }

    @Test
    public void handleUpdateUserProfile_ReturnsValidResponseEntity() throws Exception {
        var requestBuilder = MockMvcRequestBuilders.post("/api/edit")
                .with(httpBasic("+7987654321", "password1"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "firstName":"Иван",
                            "lastName":"Иванов",
                            "patronymic":"Иванович",
                            "gender":"MALE",
                            "email":"ivan@mail.ru",
                            "birthday":"2001-11-01"
                        }
                        """);

        mockMvc.perform(requestBuilder)
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                {
                                    "login": "+7987654321",
                                    "firstName": "Иван",
                                    "lastName": "Иванов",
                                    "patronymic": "Иванович",
                                    "gender": "MALE",
                                    "email": "ivan@mail.ru",
                                    "birthday": "2001-11-01"
                                }
                                """)
                );
    }

}
