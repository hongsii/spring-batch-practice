package dev.hongsii.springbatchpractice.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@NoArgsConstructor
@Getter
public class AccountHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long accountId;
    private String email;
    private String password;

    public AccountHistory(Account account) {
        this.accountId = account.getId();
        this.email = account.getEmail();
        this.password = account.getPassword();
    }
}

