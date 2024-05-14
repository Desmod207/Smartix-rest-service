package com.example.services;

import com.example.api.dto.PaymentData;
import com.example.api.dto.UserData;
import com.example.api.dto.UserRegisterData;
import com.example.data.ApplicationUserRepository;
import com.example.data.PaymentRepository;
import com.example.entities.ApplicationUser;
import com.example.entities.Gender;
import com.example.entities.Payment;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    private static final long START_BALANCE = 100000L;

    private final ApplicationUserRepository applicationUserRepository;

    private final PaymentRepository paymentRepository;

    private final PasswordEncoder passwordEncoder;

    public AccountService(ApplicationUserRepository applicationUserRepository, PaymentRepository paymentRepository, PasswordEncoder passwordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<ApplicationUser> registrationNewUser(UserRegisterData registerData) {
        Optional<ApplicationUser> optionalUser = applicationUserRepository.findByLogin(registerData.login());
        if (optionalUser.isEmpty()) {
            ApplicationUser user = new ApplicationUser();
            user.setLogin(registerData.login());
            var encodePassword = passwordEncoder.encode(registerData.password());
            user.setPassword(encodePassword);
            user.setBalance(START_BALANCE);
            return Optional.of(applicationUserRepository.save(user));
        } else {
            return Optional.empty();
        }
    }

    public String makingPayment(ApplicationUser user, PaymentData paymentData) {
        long amount = (long)(paymentData.amount() * 100);
        long newBalance = user.getBalance() - amount;
        if (newBalance >= 0) {
            Payment payment = new Payment();
            payment.setDate(new Date());
            payment.setPhone(paymentData.phone());
            payment.setAmount(amount);
            payment.setUser(user);
            paymentRepository.save(payment);

            user.setBalance(newBalance);
            applicationUserRepository.update(user);
            return "Payment is success";
        } else
            return "Not enough funds";
    }

    public List<Payment> history(ApplicationUser user, PageRequest pageRequest) {
        return paymentRepository.findAllByUser(user, pageRequest);
    }

    public ApplicationUser updateUserProfile(ApplicationUser user, UserData userData) {
        if (userData.firstName() != null) {
            user.setFirstName(userData.firstName());
        }
        if (userData.lastName() != null) {
            user.setLastName(userData.lastName());
        }
        if (userData.patronymic() != null) {
            user.setPatronymic(userData.patronymic());
        }
        if (userData.email() != null) {
            user.setEmail(userData.email());
        }
        if (userData.gender() != null) {
            user.setGender(userData.gender());
        }
        if (userData.birthday() != null) {
            user.setBirthday(userData.birthday());
        }
        return applicationUserRepository.update(user);
    }

}
