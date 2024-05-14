package com.example.services;

import com.example.api.dto.PaymentData;
import com.example.api.dto.UserData;
import com.example.api.dto.UserRegisterData;
import com.example.data.ApplicationUserRepository;
import com.example.data.PaymentRepository;
import com.example.entities.ApplicationUser;
import com.example.entities.Gender;
import com.example.entities.Payment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    ApplicationUserRepository applicationUserRepository;

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    PasswordEncoder encoder;

    @InjectMocks
    AccountService service;

    @Test
    public void registrationNewUser_ReturnsNotEmptyOptional() {
        var registerData = new UserRegisterData("login", "password");
        var applicationUser = new ApplicationUser(1L, "login", "password", 100000L);

        Mockito.doReturn(Optional.empty()).when(applicationUserRepository)
                .findByLogin(registerData.login());
        Mockito.doReturn(applicationUser).when(applicationUserRepository).save(applicationUser);
        Mockito.doReturn("password").when(encoder).encode(any());

        Optional<ApplicationUser> optionalUser = service.registrationNewUser(registerData);

        assertTrue(optionalUser.isPresent());
        assertEquals(applicationUser, optionalUser.get());

        Mockito.verify(applicationUserRepository).findByLogin(applicationUser.getLogin());
        Mockito.verify(applicationUserRepository).save(applicationUser);
        Mockito.verifyNoMoreInteractions(applicationUserRepository);
    }

    @Test
    public void registrationNewUser_ReturnsEmptyOptional() {
        var registerData = new UserRegisterData("login", "password");
        var applicationUser = new ApplicationUser(1L, "login", "password", 100000L);

        Mockito.doReturn(Optional.of(applicationUser)).when(applicationUserRepository)
                .findByLogin(applicationUser.getLogin());

        Optional<ApplicationUser> optionalUser = service.registrationNewUser(registerData);

        assertFalse(optionalUser.isPresent());

        Mockito.verify(applicationUserRepository).findByLogin(any());
        Mockito.verifyNoMoreInteractions(applicationUserRepository);
    }

    @Test
    public void makingPayment_FoundsEnough_ReturnsResponseString() {
        var applicationUser = new ApplicationUser(1L, "login1", "password1", 100000L);
        var payment = new Payment(1L, new Date(), "+79876543210", 15000, applicationUser);
        var paymentData = new PaymentData(payment);

        String responseString = service.makingPayment(applicationUser, paymentData);

        assertNotNull(responseString);
        assertEquals("Payment is success", responseString);

        Mockito.verify(paymentRepository).save(any());
        Mockito.verify(applicationUserRepository).update(any());
        Mockito.verifyNoMoreInteractions(applicationUserRepository);
        Mockito.verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    public void makingPayment_FoundsNotEnough_ReturnsResponseString() {
        var applicationUser = new ApplicationUser(1L, "login1", "password1", 100000L);
        var payment = new Payment(1L, new Date(), "+79876543210", 150000, applicationUser);
        var paymentData = new PaymentData(payment);

        String responseString = service.makingPayment(applicationUser, paymentData);

        assertNotNull(responseString);
        assertEquals("Not enough funds", responseString);

        Mockito.verifyNoInteractions(applicationUserRepository);
        Mockito.verifyNoInteractions(paymentRepository);
    }

    @Test
    public void history_ReturnsValidPaymentList() {
        var applicationUser = new ApplicationUser(1L, "login", "password", 100000L);
        var pageRequest = PageRequest.of(0, 5, Sort.by("id"));
        var payments = List.of(
                new Payment(1L, new Date(), "+79876543210", 1500, applicationUser),
                new Payment(2L, new Date(), "+79876543210", 1500, applicationUser)
        );

        Mockito.doReturn(payments).when(paymentRepository).findAllByUser(applicationUser, pageRequest);

        var returnsPayments = service.history(applicationUser, pageRequest);

        assertNotNull(returnsPayments);
        assertEquals(payments, returnsPayments);

        Mockito.verify(paymentRepository).findAllByUser(any(), any());
        Mockito.verifyNoMoreInteractions(applicationUserRepository);
        Mockito.verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    public void updateUserProfile_ReturnsValidApplicationUser() {
        var applicationUser = new ApplicationUser(1L, "login", "password", 100000L);
        applicationUser.setFirstName("Иван");
        applicationUser.setLastName("Иванов");
        applicationUser.setPatronymic("Иванович");
        applicationUser.setEmail("ivan@mail.ru");
        applicationUser.setGender(Gender.MALE);
        applicationUser.setBirthday(java.sql.Date.valueOf("2001-11-1"));
        var userData = new UserData(applicationUser);

        Mockito.doReturn(applicationUser).when(applicationUserRepository).update(applicationUser);

        var updateUser = service.updateUserProfile(applicationUser, userData);

        assertNotNull(updateUser);
        assertEquals(applicationUser, updateUser);

        Mockito.verify(applicationUserRepository).update(any());
        Mockito.verifyNoMoreInteractions(applicationUserRepository);
    }

}