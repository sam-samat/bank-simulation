package com.cydeo.service.impl;

import com.cydeo.enums.AccountType;
import com.cydeo.exception.AccountOwnershipException;
import com.cydeo.exception.BadRequestException;
import com.cydeo.exception.BalanceNotSufficientException;
import com.cydeo.exception.UnderConstructionException;
import com.cydeo.model.Account;
import com.cydeo.model.Transaction;
import com.cydeo.repository.AccountRepository;
import com.cydeo.repository.TransactionRepository;
import com.cydeo.service.TransactionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class TransactionServiceImpl implements TransactionService {

    @Value("${under_construction}")
    private boolean underConstruction;

    AccountRepository accountRepository;
    TransactionRepository transactionRepository;

    public TransactionServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction makeTransfer(BigDecimal amount, Date creationDate,
                                    Account sender, Account receiver,
                                    String message) {
        if (!underConstruction) {
            checkAccountOwnership(sender, receiver);
            validateAccount(sender, receiver);
            executeAndUpdateIfRequired(amount, sender, receiver);
            return transactionRepository.save(Transaction.builder()
                    .amount(amount)
                    .creationDate(creationDate)
                    .sender(sender.getId())
                    .receiver(receiver.getId())
                    .message(message)
                    .build());
        } else {
            throw new UnderConstructionException("Make transfer is not possible for now. Please try again later");
        }
    }

    private void executeAndUpdateIfRequired(BigDecimal amount, Account sender, Account receiver) {
        if (checkSenderBalance(sender, amount)) {
            sender.setBalance(sender.getBalance().subtract(amount));
            receiver.setBalance(receiver.getBalance().add(amount));
        } else {
            throw new BalanceNotSufficientException("Balance is not enough");
        }
    }

    private boolean checkSenderBalance(Account sender, BigDecimal amount) {
        return sender.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) > 0;
    }

    private void validateAccount(Account sender, Account receiver) {
        if (sender == null || receiver == null) {
            throw new BadRequestException("Sender ot receiver can not be null");
        }
    }

    private void checkAccountOwnership(Account sender, Account receiver) {
        if ((sender.getAccountType().equals(AccountType.SAVING) || receiver.getAccountType().equals(AccountType.SAVING))
                && !sender.getUserId().equals(receiver.getUserId())) {
            throw new AccountOwnershipException("When one of the account type is SAVING, sender and receiver has to be same person");
        }
        if (sender.getId().equals(receiver.getId())) {
            throw new BadRequestException("Sender account needs to be different from receiver account");
        }
        findAccountById(sender.getId());
        findAccountById(receiver.getId());
    }

    private void findAccountById(UUID accountId) {
        accountRepository.findById(accountId);
    }

    @Override
    public List<Transaction> findAll() {

        return transactionRepository.findAll();
    }
}
