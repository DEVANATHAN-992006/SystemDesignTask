
package com.rest.cli;

import com.rest.models.*;
import com.rest.models.enums.*;
import com.rest.repository.InMemoryStore;
import com.rest.service.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class WalletCLI {

    private final Scanner scanner = new Scanner(System.in);
    private final InMemoryStore store;
    private final UserService userService;
    private final AccountService accountService;
    private final TransferService transferService;
    private final TransactionService transactionService;

    private int currentUserId = -1;

    public WalletCLI(
            InMemoryStore store,
            UserService userService,
            AccountService accountService,
            TransferService transferService,
            TransactionService transactionService) {

        this.store = store;
        this.userService = userService;
        this.accountService = accountService;
        this.transferService = transferService;
        this.transactionService = transactionService;
    }

    public void start() {
        while (true) {
            System.out.println("\n===== DIGITAL WALLET =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose option: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    register();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    System.out.println("Thank you!");
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void register() {
        try {
            System.out.print("Name: ");
            String name = scanner.nextLine();

            System.out.print("Phone (10 digits): ");
            String phone = scanner.nextLine();

            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("4-digit PIN: ");
            String pin = scanner.nextLine();

            Customer customer = userService.registerCustomer(
                    name, phone, email, pin);

            System.out.println("Registration successful!");
            System.out.println("Your User ID: " + customer.getUserId());

        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void login() {
        try {
            System.out.print("Phone: ");
            String phone = scanner.nextLine();

            User user = userService.getUserByPhone(phone);

            if (!(user instanceof Customer)) {
                System.out.println("Customer login only.");
                return;
            }

            userService.validateActiveUser(user);

            System.out.print("PIN: ");
            String pin = scanner.nextLine();

            userService.validatePin(user, pin);

            currentUserId = user.getUserId();
            customerMenu();

        } catch (RuntimeException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private void customerMenu() {
        while (currentUserId != -1) {
            System.out.println("\n===== WALLET MENU =====");
            System.out.println("1. View Balance");
            System.out.println("2. Link Bank Account");
            System.out.println("3. Add Money");
            System.out.println("4. Withdraw Money");
            System.out.println("5. Transfer Money");
            System.out.println("6. Transaction History");
            System.out.println("7. Logout");
            System.out.print("Choose option: ");

            int choice = readInt();

            switch (choice) {
                case 1:
                    viewBalance();
                    break;
                case 2:
                    linkBankAccount();
                    break;
                case 3:
                    addMoney();
                    break;
                case 4:
                    withdrawMoney();
                    break;
                case 5:
                    transferMoney();
                    break;
                case 6:
                    transactionHistory();
                    break;
                case 7:
                    currentUserId = -1;
                    System.out.println("Logged out.");
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void viewBalance() {
        Wallet wallet = store.getWalletByUserId(currentUserId);

        if (wallet != null) {
            System.out.println("Wallet Balance: ₹" + wallet.getBalance());
        }
    }

    private void linkBankAccount() {
        try {
            System.out.println("Available Banks:");

            for (Bank bank : store.getAllBanks()) {
                System.out.println(
                        bank.getBankId() + ". " + bank.getBankName());
            }

            System.out.print("Bank ID: ");
            int bankId = readInt();

            System.out.print("Bank account number: ");
            String accountNumber = scanner.nextLine();

            System.out.print("Initial bank balance: ");
            BigDecimal balance = readAmount();

            BankAccount account = accountService.linkBankAccount(
                    currentUserId, bankId, accountNumber, balance);

            System.out.println("Bank account linked successfully.");
            System.out.println("Account ID: " + account.getAccountId());

        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void addMoney() {
        try {
            showBankAccounts();

            System.out.print("Bank Account ID: ");
            int accountId = readInt();

            System.out.print("Amount: ");
            BigDecimal amount = readAmount();

            AddMoneyTransaction tx = accountService.addMoney(
                    currentUserId, accountId, amount);

            System.out.println("Transaction Status: " + tx.getStatus());

        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void withdrawMoney() {
        try {
            showBankAccounts();

            System.out.print("Bank Account ID: ");
            int accountId = readInt();

            System.out.print("Amount: ");
            BigDecimal amount = readAmount();

            WithdrawTransaction tx = accountService.withdrawMoney(
                    currentUserId, accountId, amount);

            System.out.println("Transaction Status: " + tx.getStatus());

        } catch (RuntimeException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void transferMoney() {
        try {
            System.out.print("Receiver phone: ");
            String receiverPhone = scanner.nextLine();

            System.out.print("Amount: ");
            BigDecimal amount = readAmount();

            System.out.print("PIN: ");
            String pin = scanner.nextLine();

            TransferTransaction tx = transferService.transfer(
                    currentUserId, receiverPhone, amount, pin);

            System.out.println("Reference: " + tx.getReferenceId());
            System.out.println("Status: " + tx.getStatus());

        } catch (RuntimeException e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }

    private void transactionHistory() {
        List<Transaction> history =
                transactionService.getHistory(currentUserId);

        if (history.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        for (Transaction tx : history) {
            System.out.println(tx);
        }
    }

    private void showBankAccounts() {
        List<BankAccount> accounts =
                accountService.getLinkedBankAccounts(currentUserId);

        if (accounts.isEmpty()) {
            System.out.println("No linked bank accounts.");
            return;
        }

        for (BankAccount account : accounts) {
            System.out.println(account);
        }
    }

    private int readInt() {
        try {
            int value = Integer.parseInt(scanner.nextLine().trim());
            return value;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private BigDecimal readAmount() {
        try {
            return new BigDecimal(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
