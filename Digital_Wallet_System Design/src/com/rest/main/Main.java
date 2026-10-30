
package com.rest.main;

import com.rest.cli.WalletCLI;
import com.rest.models.Bank;
import com.rest.repository.InMemoryStore;
import com.rest.service.*;

public class Main {

    public static void main(String[] args) {

        // Create in-memory storage
        InMemoryStore store = new InMemoryStore();

        // Create services
        UserService userService = new UserService(store);

        AccountService accountService =
                new AccountService(store);

        TransferService transferService =
                new TransferService(store, userService);

        TransactionService transactionService =
                new TransactionService(store);

        RewardService rewardService =
                new RewardService(store);

        DisputeService disputeService =
                new DisputeService(store);

        // Add sample banks
        store.addBank(new Bank(store.generateBankId(), "SBI"));
        store.addBank(new Bank(store.generateBankId(), "HDFC Bank"));
        store.addBank(new Bank(store.generateBankId(), "ICICI Bank"));

        // Start console application
        WalletCLI cli = new WalletCLI(
                store,
                userService,
                accountService,
                transferService,
                transactionService
        );

        System.out.println("Welcome to The Digital Wallet and UPI Payment App!");

        cli.start();
    }
}
