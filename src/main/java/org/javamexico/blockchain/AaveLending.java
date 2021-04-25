package org.javamexico.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.ierc20.IERC20;
import org.web3j.ilendingpool.ILendingPool;
import org.web3j.iprotocoldataprovider.IProtocolDataProvider;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;


public class AaveLending {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    private String infuraId = System.getenv("INFURA_ID");
    private Web3j web3j = Web3j.build(new HttpService("https://kovan.infura.io/v3/" + infuraId ));
    private static final String TUSD_ADDRESS = "0x016750AC630F711882812f24Dba6c95b9D35856d";
    private static final String LENDING_POOL_ADDRESS = "0xE0fBa4Fc209b4948668006B2bE61711b7f465bAe";
    private static final String PROTOCOL_DATA_PROVIDER_ADDRESS = "0x3c73A5E5785cAC854D468F727c606C07488a29D6";

    public void run(){
        try {
            IERC20 tusd = IERC20.load(TUSD_ADDRESS, web3j, account(), getGasProvider());
            BigInteger balance = tusd.balanceOf(account().getAddress()).send();
            logger.info("TUSD Balance: {}", tusd.balanceOf(account().getAddress()).send());

            //Get Aave TUSD Liquidity Pool
            IProtocolDataProvider protocolDataProvider =
                    IProtocolDataProvider.load(PROTOCOL_DATA_PROVIDER_ADDRESS, web3j, account(), getGasProvider());
            BigInteger tusdLiquidity = protocolDataProvider.getReserveData(TUSD_ADDRESS).send().component1();

            logger.info("Current Aave TUSD liquidity: {}", tusdLiquidity);

            //Lending TUSD to Aave
            BigInteger tusdAmountToLend = BigInteger.TEN.multiply(BigInteger.TEN.pow(18));
            ILendingPool lendingPool =
                    ILendingPool.load(LENDING_POOL_ADDRESS, web3j, account(), getGasProvider());

            //Approve TUSD spending
            String tusdApprovalTransactionHash = tusd.approve(lendingPool.getContractAddress(), tusdAmountToLend).send().getTransactionHash();
            logger.info("TUSD Approval transaction hash: {} ", tusdApprovalTransactionHash);

            //Deposit
            String depositTransactionHash = lendingPool.deposit(TUSD_ADDRESS,  //Asset address
                                tusdAmountToLend, //Amount
                                account().getAddress(), // on behalf of
                                BigInteger.ZERO // referral code
                                ).send().getTransactionHash();

            logger.info("Deposit Transaction hash: {}", depositTransactionHash);


            //Withdraw
            /*String aTUSDAddress = protocolDataProvider.getReserveTokensAddresses(TUSD_ADDRESS).send().component1();
            IERC20 aTUSD = IERC20.load(aTUSDAddress, web3j, account(), getGasProvider());

            String atusdApprovalTransactionHash = aTUSD.approve(lendingPool.getContractAddress(), tusdAmountToLend).send().getTransactionHash();
            logger.info("aTUSD Approval transaction hash: {} ", atusdApprovalTransactionHash);
            String withdrawalTransactionHash = lendingPool.withdraw(TUSD_ADDRESS, //Asset address
                                tusdAmountToLend, //Amount to withdraw
                                account().getAddress() //To
            ).send().getTransactionHash();
            logger.info("Withdrawal Transaction hash: {}", withdrawalTransactionHash);*/

        } catch (Exception e){
            logger.error(e.getMessage());
        }
    }


    private Credentials account() {
        return Credentials.create(System.getenv("JVMMX_KEY"));
    }


    private ContractGasProvider getGasProvider() throws IOException {
        EthBlock.Block block = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock();
        BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
        BigInteger gasLimit = BigInteger.valueOf(500000L); // https://docs.aave.com/developers/getting-started/gas-limits
        return new StaticGasProvider(gasPrice, gasLimit);
    }


}
