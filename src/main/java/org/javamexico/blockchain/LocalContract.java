package org.javamexico.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.greeter.Greeter;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

public class LocalContract {

    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public void run(){
        Web3j web3j = Web3j.build(new HttpService("http://127.0.0.1:7545"));
        try {
            EthBlock.Block block = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock();
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            BigInteger gasLimit = block.getGasLimit();
            StaticGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
            Greeter contract = Greeter.deploy(web3j, getCredentialsFromPrivateKey(), gasProvider,
                    "Hello, I wanna play a game!").send();

            String currentGreeting = contract.greet().send();

            logger.info("Initial Greeting: {}", currentGreeting);

            TransactionReceipt receipt = contract.changeGreeting("Hello this is another greeting").send();
            logger.info("Gas used: {}", receipt.getGasUsed());
            logger.info("Current gas price: {}", web3j.ethGasPrice().send().getGasPrice());

            currentGreeting = contract.greet().send();
            logger.info("New Greeting: {}", currentGreeting);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void transferEther(Web3j web3j) {
        TransactionManager txManager = new RawTransactionManager(
                web3j,
                getCredentialsFromPrivateKey()
        );
        Transfer transfer = new Transfer(web3j, txManager);
        try {
            TransactionReceipt txReceipt = transfer.sendFunds(
                    "0xA5dbDd4c52dc08Fc9c0e91Ad0dCDeC111C492168",
                    BigDecimal.ONE,
                    Convert.Unit.ETHER).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Credentials getCredentialsFromPrivateKey() {
        return Credentials.create("{local blockchain private key}");
    }


}
