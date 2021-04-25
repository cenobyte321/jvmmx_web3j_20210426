package org.javamexico.blockchain;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.ierc20.IERC20;
import org.web3j.jvmmxtoken.JVMMXToken;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;


public class LocalERC20 {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private Web3j web3j = Web3j.build(new HttpService("http://127.0.0.1:7545"));

    public void run(){
        try {
            //Deploy JVMMX ERC20 Token. The contract gives 2000 tokens
            JVMMXToken jvmmxToken = JVMMXToken.deploy(web3j, admin(), getGasProvider()).send();
            logger.info("Contract address: {}", jvmmxToken.getContractAddress());
            printBalances(jvmmxToken);

            //We are going to 10 JVMMX Tokens to the user
            logger.info("Transfering 10 tokens to user");
            jvmmxToken.transfer(user().getAddress(), BigInteger.TEN.multiply(getDecimalsFactor(jvmmxToken))).send();
            printBalances(jvmmxToken);

            //We are going to transfer 1 token from user to the admin. Notice we are using the IERC interface for this
            logger.info("Transfering 1 token to admin");
            IERC20 jvmmxTokenInterface = IERC20.load(jvmmxToken.getContractAddress(), web3j, user(), getGasProvider());
            jvmmxTokenInterface.transfer(admin().getAddress(), BigInteger.ONE.multiply(getDecimalsFactor(jvmmxToken))).send();
            printBalances(jvmmxToken);

            //We are going to try to call the protected faucet method (onlyOwner)
            //jvmmxToken = JVMMXToken.load(jvmmxToken.getContractAddress(), web3j, user(), getGasProvider());
            //jvmmxToken.faucet(user().getAddress(), BigInteger.ONE).send();

            logger.info("Getting all transfers to admin address");
            Disposable transferEvent = jvmmxToken.transferEventFlowable(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST)
                    .filter(e -> e.to.equals(admin().getAddress()))
                    .doOnEach(event -> {
                        logger.info("From: {}, Amount: {}", event.getValue().from, event.getValue().value);
                    })
                    .subscribe();

            transferEvent.dispose();

        } catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    private void printBalances(JVMMXToken jvmmxToken) throws Exception {
        BigInteger adminBalance = jvmmxToken.balanceOf(admin().getAddress()).send();
        adminBalance = adminBalance.divide(getDecimalsFactor(jvmmxToken));

        BigInteger userBalance = jvmmxToken.balanceOf(user().getAddress()).send();
        userBalance = userBalance.divide(getDecimalsFactor(jvmmxToken));

        logger.info("Admin has {} JVMMX Tokens", adminBalance);
        logger.info("User has {} JVMMX Tokens", userBalance);
    }

    private BigInteger getDecimalsFactor(JVMMXToken jvmmxToken) throws Exception {
        return BigInteger.TEN.pow(jvmmxToken.decimals().send().intValue());
    }

    private Credentials admin() {
        return Credentials.create("{admin private key here}");
    }

    private Credentials user() {
        return Credentials.create("{user private key here}");
    }

    private ContractGasProvider getGasProvider() throws IOException {
        EthBlock.Block block = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock();
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        BigInteger gasLimit = block.getGasLimit();
        return new StaticGasProvider(gasPrice, gasLimit);
    }

}
