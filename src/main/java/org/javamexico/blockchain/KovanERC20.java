package org.javamexico.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.ierc20.IERC20;
import org.web3j.jvmmxtoken.JVMMXToken;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;


public class KovanERC20 {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private String infuraId = System.getenv("INFURA_ID");
    private Web3j web3j = Web3j.build(new HttpService("https://kovan.infura.io/v3/" + infuraId));
    private static final String TUSD_ADDRESS = "0x016750AC630F711882812f24Dba6c95b9D35856d";

    public void run(){
        try {
            logger.info("Deploying ERC20 contract");
            JVMMXToken jvmmxToken = JVMMXToken.deploy(web3j, account(), getGasProvider()).send();
            logger.info(jvmmxToken.getContractAddress());
        } catch (Exception e){
            logger.error(e.getMessage());
        }
    }


    private Credentials account() {
        return Credentials.create(System.getenv("JVMMX_PRIVATE_KEY"));
    }


    private ContractGasProvider getGasProvider() throws IOException {
        EthBlock.Block block = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock();
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        BigInteger gasLimit = block.getGasLimit();
        return new StaticGasProvider(gasPrice, gasLimit);
    }


}
