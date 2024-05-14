package com.example.api;

import com.example.api.dto.PaymentData;
import com.example.api.dto.UserData;
import com.example.api.dto.UserRegisterData;
import com.example.entities.ApplicationUser;
import com.example.api.dto.Balance;
import com.example.entities.Payment;

import com.example.services.AccountService;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService applicationUserService) {
        this.accountService = applicationUserService;
    }

    @PostMapping("/api/registration")
    @Transactional
    public ResponseEntity<?> handleRegistrationNewUser(@RequestBody UserRegisterData registerData) {
        if (registerData.login() == null || registerData.password() == null ||
                registerData.login().trim().equals("") || registerData.password().trim().equals("")) {
            return ResponseEntity.badRequest().body("Login and password required");
        }
        if (!registerData.login().matches("[0-9+]+")) {
            return ResponseEntity.badRequest().body("Login must be phone number");
        }

        var optionalUser = accountService.registrationNewUser(registerData);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User with this login already exists");
        } else {
            return ResponseEntity.created(URI.create("/api/profile"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new UserData(optionalUser.get()));
        }
    }

    @GetMapping("/api/balance")
    public ResponseEntity<Balance> balance(@AuthenticationPrincipal ApplicationUser user) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Balance(user));
    }

    @Transactional
    @PostMapping("/api/payment")
    public ResponseEntity<String> handleMakingPayment(
            @AuthenticationPrincipal ApplicationUser user,
            @RequestBody PaymentData paymentData) {
        if (!paymentData.phone().matches("[0-9+]+")) {
            return ResponseEntity.badRequest().body("Phone number must contain only numbers");
        }
        if (paymentData.amount() <= 0) {
            return ResponseEntity.badRequest().body("Amount must be positive");
        } else {
            return ResponseEntity.ok(accountService.makingPayment(user, paymentData));
        }
    }

    @GetMapping("/api/history")
    public ResponseEntity<List<PaymentData>> handleHistory(
            @RequestParam(value = "page", required = false , defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10" ) int size,
            @AuthenticationPrincipal ApplicationUser user) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());
        List<Payment> payments = accountService.history(user, pageRequest);

        List<PaymentData> paymentDataList = new ArrayList<>();
        for (Payment payment : payments) {
            paymentDataList.add(new PaymentData(payment));
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(paymentDataList);
    }

    @Transactional
    @PostMapping("/api/edit")
    public ResponseEntity<UserData> handleUpdateUserProfile(
            @AuthenticationPrincipal ApplicationUser user,
            @RequestBody UserData userData) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UserData(accountService.updateUserProfile(user, userData)));
    }

}
