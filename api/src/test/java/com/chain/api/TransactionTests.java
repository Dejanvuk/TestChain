package com.chain.api;

import com.chain.api.core.Block.Block;
import com.chain.api.core.Crypto.CryptoUtil;
import com.chain.api.core.Transaction.*;
import com.chain.api.core.Wallet.WalletUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTests {

    private List<UTXO> utxos;

    private List<UTXO> utxosOtherNode;

    private PublicKey publicKeySender;
    private PrivateKey privateKeySender;

    private PublicKey publicKeyReceiver;
    private PrivateKey privateKeyReceiver;

    private PublicKey publicKeyThirdUser;
    private PrivateKey privateKeyThirdUser;

    private UnconfirmedTransactions unconfirmedTransactionsSender;
    private UnconfirmedTransactions unconfirmedTransactionsReceiver;

    @BeforeEach
    public void setup() {
        System.out.println("=============== Start of Test Setup ===============\n");

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        utxos = new ArrayList<>();
        utxosOtherNode = new ArrayList<>();

        unconfirmedTransactionsSender = new UnconfirmedTransactions();
        unconfirmedTransactionsReceiver = new UnconfirmedTransactions();
        // generate random wallets for sender and receiver and for a third user
        KeyPair keyPair = WalletUtil.generateKeyPair();
        publicKeySender = keyPair.getPublic();
        privateKeySender = keyPair.getPrivate();

        keyPair = WalletUtil.generateKeyPair();
        publicKeyReceiver = keyPair.getPublic();
        privateKeyReceiver = keyPair.getPrivate();

        keyPair = WalletUtil.generateKeyPair();
        publicKeyThirdUser = keyPair.getPublic();
        privateKeyThirdUser = keyPair.getPrivate();

        System.out.println("public key sender:      " + CryptoUtil.getStringFromKey(publicKeySender));

        System.out.println("public key receiver     " + CryptoUtil.getStringFromKey(publicKeyReceiver));

        System.out.println("public key third user   " + CryptoUtil.getStringFromKey(publicKeyThirdUser));

        System.out.println("=============== End of Test Setup ===============\n");
    }

    /**
     *
     */
    @Test
    public void verifyCoinbaseTransactions() {
        Transaction transaction1 = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, 0);
        assertTrue(TransactionUtil.verifyCoinbaseTransaction(transaction1, 0));
    }

    /**
     * Verifies the creation of transactions
     */
    @Test
    public void verifyTransaction() {
        // Sender mines a block to get some coins
        Transaction transaction1 = TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, 0);
        /*
        System.out.println("Before sending a transaction");
        utxos.stream().forEach(utxo -> {
            System.out.println("owner:" + CryptoUtil.getStringFromKey(utxo.getOwner()) + " value:" + utxo.getValue());
        });
        */
        assertTrue(utxos.size() == 1);
        assertEquals(CryptoUtil.getStringFromKey(utxos.get(0).getOwner()),CryptoUtil.getStringFromKey(publicKeySender));

        // Send 5 coins to receiver
        try {
            Transaction transaction2 = TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender), CryptoUtil.getStringFromKey(publicKeyReceiver),5,utxos,unconfirmedTransactionsSender.getTransactions(), 0);
            assertTrue(utxos.size() == 1);
            assertTrue(unconfirmedTransactionsSender.getTransactions().size() == 1);
            assertEquals(CryptoUtil.getStringFromKey(utxos.get(0).getOwner()),CryptoUtil.getStringFromKey(publicKeyReceiver));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        // Send 5 coins back to sender
        try {
            Transaction transaction3 = TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeyReceiver), CryptoUtil.getStringFromKey(publicKeySender),5,utxos,unconfirmedTransactionsSender.getTransactions(), 0);
            assertTrue(utxos.size() == 1);
            assertTrue(unconfirmedTransactionsSender.getTransactions().size() == 2);
            assertEquals(CryptoUtil.getStringFromKey(utxos.get(0).getOwner()),CryptoUtil.getStringFromKey(publicKeySender));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        // Send 6 coins to receiver when sender has only 5
        RuntimeException exception = assertThrows(RuntimeException.class, () -> TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender), CryptoUtil.getStringFromKey(publicKeyReceiver),6,utxos,unconfirmedTransactionsSender.getTransactions(),0));
        assertEquals("Sender does not have enough funds", exception.getMessage());
        assertTrue(unconfirmedTransactionsSender.getTransactions().size() == 2);

    }

    /**
     * verifies the sending of multiple transactions
     */
    @Test
    public void sendTransactionsTest() {
        List<Transaction> transactions = new ArrayList<>();
        // Create 3 transactions of 12 coins total to sender
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),4, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),3, 0));

        // Create 2 transactions of 17 coins total to receiver
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeyReceiver),9, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeyReceiver),8, 0));

        try {
            // Try sending 2 coins from sender to receiver
            transactions.add(TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender),CryptoUtil.getStringFromKey(publicKeyReceiver), 2, utxos,unconfirmedTransactionsSender.getTransactions(), 0));

            // the first transaction of 5 coins was split in 2 coins that were sent to receiver and 3 coins that were sent back to sender
            // so sender has 3 utxos and receiver received 1 extra utxo,making it 3
            // the utxo value order for sender is 4 3 3 and the utxo order for receiver is 9 8 2
            assertEquals(3, TransactionUtil.getUserUtxos(publicKeySender,utxos).size());
            assertEquals(3, TransactionUtil.getUserUtxos(publicKeyReceiver,utxos).size());
            assertEquals(12 - 2, TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(publicKeySender,utxos)));
            assertEquals(17 + 2, TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(publicKeyReceiver,utxos)));

            // Try sending all 19 coins of receiver to third user
            transactions.add(TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeyReceiver),CryptoUtil.getStringFromKey(publicKeyThirdUser), 19, utxos,unconfirmedTransactionsSender.getTransactions(), 0));

            assertEquals(3, TransactionUtil.getUserUtxos(publicKeySender,utxos).size());
            assertEquals(0, TransactionUtil.getUserUtxos(publicKeyReceiver,utxos).size());
            assertEquals(1, TransactionUtil.getUserUtxos(publicKeyThirdUser,utxos).size());
            assertEquals(10, TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(publicKeySender,utxos)));
            assertEquals(19 - 19, TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(publicKeyReceiver,utxos)));
            assertEquals(0 + 19, TransactionUtil.getUsersBalance(TransactionUtil.getUserUtxos(publicKeyThirdUser,utxos)));


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test the locking and verification methods of Transaction inputs
     */
    @Test
    public void lockAndVerifyInputs() {
        String txid = "test txid";
        // give sender 15 coins
        utxos.add(new UTXO("trans0",0,publicKeySender,5));
        utxos.add(new UTXO("trans1",0,publicKeySender,7));
        utxos.add(new UTXO("trans2",0,publicKeySender,2));

        // create some TXI's to reference them
        List<TransactionInput> inputs = new ArrayList<>();
        inputs.add(new TransactionInput("trans0",0,""));
        inputs.add(new TransactionInput("trans1",0,""));
        inputs.add(new TransactionInput("trans2",0,""));

        // sign the TXI's with some gibberish txid to sender
        boolean status = TransactionUtil.lockTransactionInputs(privateKeySender,inputs,txid,utxos);

        assertTrue(status);

        // modify  a TXI to refference a non-existent transaction
        inputs.add(new TransactionInput("trans3",0,""));

        status = TransactionUtil.lockTransactionInputs(privateKeySender,inputs,txid,utxos);

        assertFalse(status);

        /*
         test the method that validates the inputs
         */

        //trans3 has a null signature so it will throw a runtime error when we try to decode the signature

        RuntimeException exception = assertThrows(RuntimeException.class, () -> TransactionUtil.verifyTransactionInputs(publicKeySender,inputs,txid));
        assertEquals("java.security.SignatureException: error decoding signature bytes.", exception.getMessage());

        inputs.remove(3); // remove the bad TXI and try again

        status = TransactionUtil.verifyTransactionInputs(publicKeySender,inputs,txid);

        assertTrue(status);
    }

    /**
     * Tests the addition of incoming block transactions
     */
    @Test
    public void updateTxosTest() {
        try {
            // Make some  coinbase transactions
            List<Transaction> transactions = new ArrayList<>();
            transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, 0));

            TransactionUtil.updateUtxos(transactions,utxos);
            TransactionUtil.updateUtxos(transactions,utxosOtherNode);

            transactions = new ArrayList<>();

            transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),4, 0));

            TransactionUtil.updateUtxos(transactions,utxos);
            TransactionUtil.updateUtxos(transactions,utxosOtherNode);

            transactions = new ArrayList<>();

            transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),3, 0));

            TransactionUtil.updateUtxos(transactions,utxos);
            TransactionUtil.updateUtxos(transactions,utxosOtherNode);

            //----------------create 2 transactions on second node------------------//

            transactions = new ArrayList<>();

            // create a transaction
            Transaction trans1 = TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender),CryptoUtil.getStringFromKey(publicKeyReceiver), 2, utxosOtherNode, unconfirmedTransactionsReceiver.getTransactions(), 1);
            unconfirmedTransactionsReceiver.getTransactions().add(trans1);
            // the transaction was mined
            TransactionUtil.updateUtxos(unconfirmedTransactionsReceiver.getTransactions(),utxosOtherNode);
            TransactionUtil.updateUnconfirmedTransactions(utxosOtherNode, unconfirmedTransactionsReceiver.getTransactions());

            // create a transaction
            Transaction trans2 = TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender),CryptoUtil.getStringFromKey(publicKeyReceiver), 3, utxosOtherNode, unconfirmedTransactionsReceiver.getTransactions(), 1);
            unconfirmedTransactionsReceiver.getTransactions().add(trans2);
            // the transaction was mined
            TransactionUtil.updateUtxos(unconfirmedTransactionsReceiver.getTransactions(),utxosOtherNode);
            TransactionUtil.updateUnconfirmedTransactions(utxosOtherNode, unconfirmedTransactionsReceiver.getTransactions());

            utxosOtherNode.stream().forEach(utxo -> System.out.println(utxo.getValue() + " owner: " + CryptoUtil.getStringFromKey(utxo.getOwner())));

            assertEquals(utxos.size(), 3);
            assertEquals(utxosOtherNode.size(), 5);

            // the transactions of the new block
            transactions.add(trans1);
            transactions.add(trans2);

            TransactionUtil.updateUtxos(transactions,utxos);

            assertEquals(utxos.size(), 5);

            for(int i = 0; i < utxos.size(); i++) {
                if(utxos.get(i).getValue() != utxosOtherNode.get(i).getValue()) fail();
            }

            //----------------create 2 transactions on second node------------------//
            
            List<Transaction> otherTransactions = new ArrayList<>();
            trans1 = TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender),CryptoUtil.getStringFromKey(publicKeyReceiver), 3, utxosOtherNode,unconfirmedTransactionsReceiver.getTransactions(),  2);
            unconfirmedTransactionsReceiver.getTransactions().add(trans1);

            // the transaction was mined
            TransactionUtil.updateUtxos(unconfirmedTransactionsReceiver.getTransactions(),utxosOtherNode);
            TransactionUtil.updateUnconfirmedTransactions(utxosOtherNode, unconfirmedTransactionsReceiver.getTransactions());

            trans2 = TransactionUtil.createTransaction(CryptoUtil.getStringFromKey(privateKeySender),CryptoUtil.getStringFromKey(publicKeyReceiver), 4, utxosOtherNode,unconfirmedTransactionsReceiver.getTransactions(),  2);
            unconfirmedTransactionsReceiver.getTransactions().add(trans2);

            TransactionUtil.updateUtxos(unconfirmedTransactionsReceiver.getTransactions(),utxosOtherNode);
            TransactionUtil.updateUnconfirmedTransactions(utxosOtherNode, unconfirmedTransactionsReceiver.getTransactions());

            // the transactions of the new block
            otherTransactions.add(trans1);
            otherTransactions.add(trans2);

            TransactionUtil.updateUtxos(otherTransactions,utxos);

            assertEquals(utxos.size(), 4);
            assertEquals(utxosOtherNode.size(), 4);

            Integer[] firstUtxos = {2,3,3,4};

            for(int i = 0; i < utxosOtherNode.size(); i++) {
                UTXO utxo = utxosOtherNode.get(i);
                if(utxo.getValue() != firstUtxos[i] || !CryptoUtil.getStringFromKey(utxo.getOwner()).equals(CryptoUtil.getStringFromKey(publicKeyReceiver)))
                fail();
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    /**
     * test the method that gets the user UTXO's
     */
    @Test
    public void getUserUtxosTest() {
        List<Transaction> transactions = new ArrayList<>();
        // Create 3 transactions of 12 coins total to sender
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),5, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),4, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeySender),3, 0));

        // Create 2 transactions of 3 coins total to receiver
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeyReceiver),2, 0));
        transactions.add(TransactionUtil.createCoinbaseTransaction(CryptoUtil.getStringFromKey(publicKeyReceiver),1, 0));

        List<UTXO> userUtxos = TransactionUtil.getUserUtxos(publicKeySender,utxos);
        Float value = TransactionUtil.getUsersBalance(userUtxos);

        // utxo's are only updated after the transaction was mined
        assertEquals(0,userUtxos.size());
        assertEquals(0,value);

        userUtxos = TransactionUtil.getUserUtxos(publicKeyReceiver,utxos);
        value = TransactionUtil.getUsersBalance(userUtxos);

        assertEquals(0,0);
        assertEquals(0,0);

    }
}
