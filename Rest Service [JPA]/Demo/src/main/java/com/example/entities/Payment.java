package com.example.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Date;

@Entity
public class Payment {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(nullable = false)
        private Date date;

        @Column(nullable = false)
        private String phone;

        @Column(nullable = false)
        private long amount;

        @ManyToOne(fetch = FetchType.LAZY)
        @JsonIgnore
        private ApplicationUser user;

        public  Payment() {}

        public Payment(Long id, Date date, String phone, long amount, ApplicationUser user) {
                this.id = id;
                this.date = date;
                this.phone = phone;
                this.amount = amount;
                this.user = user;
        }

        public Long getId() {
                return id;
        }

        public Date getDate() {
                return date;
        }

        public void setDate(Date date) {
                this.date = date;
        }

        public String getPhone() {
                return phone;
        }

        public void setPhone(String phone) {
                this.phone = phone;
        }

        public long getAmount() {
                return amount;
        }

        public void setAmount(long amount) {
                this.amount = amount;
        }

        public void setUser(ApplicationUser applicationUser) {
                this.user = applicationUser;
        }

}
