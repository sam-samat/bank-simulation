package com.cydeo.repository;

import com.cydeo.exception.RecordNotFoundException;
import com.cydeo.model.Account;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class AccountRepository {

    public static List<Account> accountList = new ArrayList<>();

    public Account saveAccount(Account account){
        accountList.add(account);
        return account;
    }

    public List<Account> findAll() {
        return accountList;
    }

    public Account findById(UUID accountId) {
        return accountList.stream()
                .filter(account -> account.getId().equals(accountId))
                .findAny()
                .orElseThrow(() -> new RecordNotFoundException("This account is not in the database"));
    }
}
