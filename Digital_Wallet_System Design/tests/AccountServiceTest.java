
import com.rest.models.*;
import com.rest.models.enums.*;
import com.rest.repository.InMemoryStore;
import com.rest.service.AccountService;

import java.math.BigDecimal;

public class AccountServiceTest {

    // Creates fresh sample data for every test
    static class TestData {
        InMemoryStore store;
        AccountService service;
        Customer customer;
        Wallet wallet;
        BankAccount bankAccount;

        TestData(KycStatus kycStatus, String bankBalance) {

            store = new InMemoryStore();
            service = new AccountService(store);

            customer = new Customer(
                    store.generateUserId(),
                    "Test User",
                    "9876543210",
                    "test@gmail.com",
                    "1234",
                    kycStatus,
                    UserStatus.ACTIVE
            );

            store.addUser(customer);

            wallet = new Wallet(
                    store.generateWalletId(),
                    customer.getUserId()
            );

            store.addWallet(wallet);

            int bankId = store.generateBankId();

            store.addBank(new Bank(bankId, "Test Bank"));

            bankAccount = service.linkBankAccount(
                    customer.getUserId(),
                    bankId,
                    "1234567890",
                    new BigDecimal(bankBalance)
            );
        }
    }

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {

        runTest("Successful Add Money",
                AccountServiceTest::successfulAddMoney);

        runTest("Add Money Below Minimum",
                AccountServiceTest::addBelowMinimum);

        runTest("Add Money Above Maximum",
                AccountServiceTest::addAboveMaximum);

        runTest("Insufficient Bank Balance",
                AccountServiceTest::insufficientBankBalance);

        runTest("Basic KYC Wallet Limit",
                AccountServiceTest::basicKycLimit);

        runTest("Full KYC Wallet Limit",
                AccountServiceTest::fullKycLimit);

        runTest("Successful Withdrawal",
                AccountServiceTest::successfulWithdrawal);

        runTest("Insufficient Wallet Balance",
                AccountServiceTest::insufficientWalletBalance);

        runTest("Blocked User Transaction",
                AccountServiceTest::blockedUser);

        System.out.println("\n========== TEST SUMMARY ==========");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
    }

    static void runTest(String name, Runnable test) {

        try {
            test.run();
            passed++;
            System.out.println("PASS: " + name);

        } catch (Throwable error) {
            failed++;
            System.out.println("FAIL: " + name);
            System.out.println("Reason: " + error.getMessage());
        }
    }

    static void checkAmount(String expected, BigDecimal actual) {

        if (new BigDecimal(expected).compareTo(actual) != 0) {
            throw new AssertionError(
                    "Expected " + expected + " but got " + actual
            );
        }
    }

    static void checkStatus(
            TransactionStatus expected,
            TransactionStatus actual) {

        if (expected != actual) {
            throw new AssertionError(
                    "Expected " + expected + " but got " + actual
            );
        }
    }

    static void checkThrows(
            Class<? extends RuntimeException> expected,
            Runnable operation) {

        try {
            operation.run();

        } catch (RuntimeException error) {

            if (expected.isInstance(error)) {
                return;
            }

            throw new AssertionError(
                    "Expected " + expected.getSimpleName()
                    + " but got " + error.getClass().getSimpleName()
            );
        }

        throw new AssertionError(
                "Expected exception: " + expected.getSimpleName()
        );
    }

    // TEST 1: Successful Add Money
    static void successfulAddMoney() {

        TestData data = new TestData(KycStatus.BASIC, "5000");

        AddMoneyTransaction tx = data.service.addMoney(
                data.customer.getUserId(),
                data.bankAccount.getAccountId(),
                new BigDecimal("1000")
        );

        checkStatus(TransactionStatus.SUCCESS, tx.getStatus());
        checkAmount("1000", data.wallet.getBalance());
        checkAmount("4000", data.bankAccount.getBalance());
    }

    // TEST 2: Below minimum amount
    static void addBelowMinimum() {

        TestData data = new TestData(KycStatus.BASIC, "5000");

        checkThrows(IllegalArgumentException.class,
                () -> data.service.addMoney(
                        data.customer.getUserId(),
                        data.bankAccount.getAccountId(),
                        new BigDecimal("5")
                ));

        checkAmount("0", data.wallet.getBalance());
        checkAmount("5000", data.bankAccount.getBalance());
    }

    // TEST 3: Above maximum amount
    static void addAboveMaximum() {

        TestData data = new TestData(KycStatus.FULL, "100000");

        checkThrows(IllegalArgumentException.class,
                () -> data.service.addMoney(
                        data.customer.getUserId(),
                        data.bankAccount.getAccountId(),
                        new BigDecimal("50001")
                ));

        checkAmount("0", data.wallet.getBalance());
        checkAmount("100000", data.bankAccount.getBalance());
    }

    // TEST 4: Insufficient bank balance
    static void insufficientBankBalance() {

        TestData data = new TestData(KycStatus.BASIC, "500");

        AddMoneyTransaction tx = data.service.addMoney(
                data.customer.getUserId(),
                data.bankAccount.getAccountId(),
                new BigDecimal("1000")
        );

        checkStatus(TransactionStatus.FAILED, tx.getStatus());
        checkAmount("0", data.wallet.getBalance());
        checkAmount("500", data.bankAccount.getBalance());

        if (data.store.getAllTransactions().size() != 1) {
            throw new AssertionError("Failed transaction was not recorded");
        }
    }

    // TEST 5: Basic KYC wallet limit
    static void basicKycLimit() {

        TestData data = new TestData(KycStatus.BASIC, "20000");

        AddMoneyTransaction tx = data.service.addMoney(
                data.customer.getUserId(),
                data.bankAccount.getAccountId(),
                new BigDecimal("10001")
        );

        checkStatus(TransactionStatus.FAILED, tx.getStatus());
        checkAmount("0", data.wallet.getBalance());
        checkAmount("20000", data.bankAccount.getBalance());
    }

    // TEST 6: Full KYC wallet limit
    static void fullKycLimit() {

        TestData data = new TestData(KycStatus.FULL, "200000");

        data.service.addMoney(
                data.customer.getUserId(),
                data.bankAccount.getAccountId(),
                new BigDecimal("50000")
        );

        data.service.addMoney(
                data.customer.getUserId(),
                data.bankAccount.getAccountId(),
                new BigDecimal("50000")
        );

        AddMoneyTransaction tx = data.service.addMoney(
                data.customer.getUserId(),
                data.bankAccount.getAccountId(),
                new BigDecimal("10")
        );

        checkStatus(TransactionStatus.FAILED, tx.getStatus());
        checkAmount("100000", data.wallet.getBalance());
        checkAmount("100000", data.bankAccount.getBalance());
    }

    // TEST 7: Successful Withdrawal
    static void successfulWithdrawal() {

        TestData data = new TestData(KycStatus.BASIC, "1000");

        data.wallet.credit(new BigDecimal("3000"));

        WithdrawTransaction tx = data.service.withdrawMoney(
                data.customer.getUserId(),
                data.bankAccount.getAccountId(),
                new BigDecimal("1000")
        );

        checkStatus(TransactionStatus.SUCCESS, tx.getStatus());
        checkAmount("2000", data.wallet.getBalance());
        checkAmount("2000", data.bankAccount.getBalance());
    }

    // TEST 8: Insufficient wallet balance
    static void insufficientWalletBalance() {

        TestData data = new TestData(KycStatus.BASIC, "1000");

        data.wallet.credit(new BigDecimal("500"));

        WithdrawTransaction tx = data.service.withdrawMoney(
                data.customer.getUserId(),
                data.bankAccount.getAccountId(),
                new BigDecimal("1000")
        );

        checkStatus(TransactionStatus.FAILED, tx.getStatus());
        checkAmount("500", data.wallet.getBalance());
        checkAmount("1000", data.bankAccount.getBalance());
    }

    // TEST 9: Blocked user
    static void blockedUser() {

        TestData data = new TestData(KycStatus.BASIC, "1000");

        data.customer.setStatus(UserStatus.BLOCKED);

        checkThrows(IllegalStateException.class,
                () -> data.service.withdrawMoney(
                        data.customer.getUserId(),
                        data.bankAccount.getAccountId(),
                        new BigDecimal("100")
                ));

        checkAmount("0", data.wallet.getBalance());
        checkAmount("1000", data.bankAccount.getBalance());
    }
}
