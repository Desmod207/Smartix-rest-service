package com.example.api;

import com.example.api.dto.PaymentData;
import com.example.api.dto.UserData;
import com.example.api.dto.UserRegisterData;
import com.example.entities.ApplicationUser;
import com.example.api.dto.Balance;
import com.example.entities.Gender;
import com.example.entities.Payment;
import com.example.services.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

    @Mock
    AccountService service;

    @InjectMocks
    AccountController controller;

    @Test
    public void handleRegistrationNewUser_ValidLoginAndPassword_ReturnsValidResponseEntity() {
        var registerData = new UserRegisterData("+79876543210", "password");
        var applicationUser = new ApplicationUser(1L, "+79876543210", "password", 100000L);
        var userData = new UserData((applicationUser));

        Mockito.doReturn(Optional.of(applicationUser)).when(service).registrationNewUser(registerData);

        var responseEntity = controller.handleRegistrationNewUser(registerData);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertEquals(userData, responseEntity.getBody());
    }

    @Test
    public void registrationNewUser_LoginIsNull_ReturnsValidResponseEntity() {
        var registerData = new UserRegisterData(null, "password");

        var responseEntity = controller.handleRegistrationNewUser(registerData);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Login and password required", responseEntity.getBody());
    }

    @Test
    public void registrationNewUser_EmptyLogin_ReturnsValidResponseEntity() {
        var registerData = new UserRegisterData("", "password");

        var responseEntity = controller.handleRegistrationNewUser(registerData);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Login and password required", responseEntity.getBody());
    }

    @Test
    public void registrationNewUser_InvalidLogin_ReturnsValidResponseEntity() {
        var registerData = new UserRegisterData("79876sda543210", "password");

        var responseEntity = controller.handleRegistrationNewUser(registerData);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Login must be phone number", responseEntity.getBody());
    }

    @Test
    public void registrationNewUser_LoginIsAlreadyExist_ReturnsValidResponseEntity() {
        var registerData = new UserRegisterData("+79876543210", "password");

        Mockito.doReturn(Optional.empty()).when(service).registrationNewUser(registerData);

        var responseEntity = controller.handleRegistrationNewUser(registerData);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("User with this login already exists", responseEntity.getBody());
    }

    @Test
    public void balance_ReturnsValidResponseEntity() {
        var applicationUser = new ApplicationUser(1L, "login", "password", 100000L);

        var responseEntity = controller.balance(applicationUser);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertEquals(new Balance(applicationUser), responseEntity.getBody());
    }

    @Test
    public void handleMakingPayment_PositiveAmount_ReturnsValidResponseEntity() {
        var applicationUser = new ApplicationUser(1L, "login", "password", 100000L);
        var payment = new Payment(1L, new Date(), "+79876543210", 1500, applicationUser);
        var paymentData = new PaymentData(payment);

        Mockito.doReturn("Payment is success").when(service).makingPayment(applicationUser, paymentData);

        var responseEntity = controller.handleMakingPayment(applicationUser, paymentData);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Payment is success", responseEntity.getBody());
    }

    @Test
    public void handleMakingPayment_NegativeAmount_ReturnsValidResponseEntity() {
        var applicationUser = new ApplicationUser(1L, "login", "password", 100000L);
        var payment = new Payment(1L, new Date(), "+79876543210", -1500, applicationUser);
        var paymentData = new PaymentData(payment);

        var responseEntity = controller.handleMakingPayment(applicationUser, paymentData);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Amount must be positive", responseEntity.getBody());
    }

    @Test
    public void handleMakingPayment_InvalidPhone_ReturnsValidResponseEntity() {
        var applicationUser = new ApplicationUser(1L, "login", "password", 100000L);
        var payment = new Payment(1L, new Date(), "+7987asd6543210", 1500, applicationUser);
        var paymentData = new PaymentData(payment);

        var responseEntity = controller.handleMakingPayment(applicationUser, paymentData);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Phone number must contain only numbers", responseEntity.getBody());
    }

    @Test
    public void handleHistory_ReturnsValidResponseEntity() {
        var applicationUser = new ApplicationUser(1L, "login", "password", 100000L);
        var pageRequest = PageRequest.of(0, 5, Sort.by("id"));
        var payments = List.of(
                new Payment(1L, new Date(), "+79876543210", 1500, applicationUser),
                new Payment(2L, new Date(), "+79876543210", 1500, applicationUser)
        );
        var paymentDataList = List.of(
                new PaymentData(new Payment(1L, new Date(), "+79876543210", 1500, applicationUser)),
                new PaymentData(new Payment(2L, new Date(), "+79876543210", 1500, applicationUser))
        );

        Mockito.doReturn(payments).when(service).history(applicationUser, pageRequest);

        var responseEntity = controller.handleHistory(0, 5, applicationUser);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertEquals(paymentDataList, responseEntity.getBody());
    }

    @Test
    public void handleUpdateUserProfile_ReturnsValidResponseEntity() {
        var applicationUser = new ApplicationUser(1L, "login", "password", 100000L);
        applicationUser.setFirstName("Иван");
        applicationUser.setLastName("Иванов");
        applicationUser.setPatronymic("Иванович");
        applicationUser.setEmail("ivan@mail.ru");
        applicationUser.setGender(Gender.MALE);
        applicationUser.setBirthday(java.sql.Date.valueOf("2001-11-1"));
        var userData = new UserData(applicationUser);

        Mockito.doReturn(applicationUser).when(service)
                .updateUserProfile(applicationUser, userData);

        var responseEntity = controller.handleUpdateUserProfile(applicationUser, userData);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());
        assertEquals(userData, responseEntity.getBody());
    }

}
