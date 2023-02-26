package com.healthive.Service;

import com.healthive.Contract.Patient;
import com.healthive.Models.User;
import com.healthive.Payloads.HealthRecordDto;
import com.healthive.Repository.UserRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class HealthRecordService {
    private final Web3j web3j;
    private final Credentials credentials;
    private final UserRepo userRepo;
    private final String contractAddress;
    BigInteger gasPrice = BigInteger.valueOf(20000000000L);
    BigInteger gasLimit = BigInteger.valueOf(6721975);
    public HealthRecordService(@Value("${ethereum.network.url}") String networkUrl,
                               @Value("${ethereum.contract.address}") String contract,
                               @Value("${ethereum.account.privateKey}") String Key, UserRepo userRepo) {
        this.web3j = Web3j.build(new HttpService(networkUrl));
        this.userRepo = userRepo;
        this.contractAddress = contract;
        this.credentials = Credentials.create(Key);
    }
    ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);
    public Patient patient(Web3j web3j) throws Exception {
        return Patient.load(contractAddress, web3j, credentials, gasProvider);
    }
    @Async
    public void addPatientRecord(User user, BigInteger serial, String email, String privateKey, String name, String location, String to, String from, String symptoms, List<String> prescriptionUrls, String description, Boolean update) throws Exception {
        System.out.println("\n\n"+credentials.getAddress());
        EthGetBalance balance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
        EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
        EthBlock ethBlock = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
        EthBlock.Block block = ethBlock.getBlock();
        BigDecimal balanceInEther = Convert.fromWei(balance.getBalance().toString(), Convert.Unit.ETHER);
        serial = serial.add(BigInteger.ONE);
        this.gasLimit = block.getGasLimit();
        this.gasPrice = ethGasPrice.getGasPrice();
        System.out.println("Wallet balance: " + balanceInEther+ " ETH");
        System.out.println("Gas Limit: " + gasLimit + " ETH");
        System.out.println("Gas Price: " + gasPrice + " ETH\n\n\n");
        String joinedPrescriptionUrls= String.join(",", prescriptionUrls);
        System.out.println(joinedPrescriptionUrls+"\n\n");
        Patient patientContract = patient(web3j);
        TransactionReceipt transactionResponse = patientContract.setNewPatientRecord(serial.toString(), email, privateKey, name, location, to, from, symptoms, description, joinedPrescriptionUrls).send();
        String transactionHash = transactionResponse.getTransactionHash();
        System.out.println("\n\nGet the transaction hash: "+ transactionHash+"\n\n");
        Optional<TransactionReceipt> receipt = null;
        do {
            TimeUnit.SECONDS.sleep(10);
            EthGetTransactionReceipt ethGetTransactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
            receipt = ethGetTransactionReceipt.getTransactionReceipt();
        } while (receipt == null);
        if(!update){
            user.setSerial(serial);
            this.userRepo.save(user);
        }
        System.out.println("Transaction Hash: " + transactionResponse.getTransactionHash());
        System.out.println("Block Number: " + transactionResponse.getBlockNumber());
        System.out.println("Gas Used: " + transactionResponse.getGasUsed());
        System.out.println("\n\n");
    }
    public HealthRecordDto getPatientRecord(BigInteger serial, String email, String privateKey) throws Exception {
        Patient patient = patient(web3j);
        Tuple7<String, String, String, String, String, String, String> record = patient.getPatientRecord(serial.toString(), email, privateKey).send();
        System.out.println("\n\n\n");
        System.out.println(record.component6());
        System.out.println("\n\n");
        String[] stringArray = record.component6().split(",");
        List<String> componentAt6 = Arrays.asList(stringArray);
        return new HealthRecordDto(serial, record.component1(), record.component2(),record.component3(),record.component4(),record.component5(),componentAt6,record.component7());
    }
}
