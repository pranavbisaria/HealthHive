package com.healthive.Service;
import com.healthive.Config.TwilioConfig;
import com.twilio.rest.api.v2010.account.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service @RequiredArgsConstructor
@Slf4j
public class OTPService {
    private final EmailService emailService;
    private final TwilioConfig twilioConfig;
    public int OTPRequest(String email){
        Random rand = new Random();
        int otpCheck = rand.nextInt(899999) +100000;
        String subject = "OTP Verification";
        String message = "Dear User," +
                "\nThe One Time Password (OTP) to verify your Email Address is " + otpCheck +
                "\nThe One Time Password is valid for the next 10 minutes."+
                "\n(This is an auto generated email, so please do not reply back.)" +
                "\nRegards," +
                "\nTeam HealthHive";
        String to = email;
        this.emailService.sendEmail(subject, message, to);
        return otpCheck;
    }
    public void SuccessRequest(String email, String name){
        String subject = "Successfully registered on HealthHive";
        String message = "Dear " + name + "," +
                "\nThank you for registering on ShopIT" +
                "\nNow enjoy sharing the data without any data loss risk, with blockchain based HealthHive"+
                "\nYour personal health care partner always ready to serve from your smartphone" +
                "\n(This is an auto generated email, so please do not reply back.)" +
                "\nRegards," +
                "\nTeam HeathHive";
        String to = email;
        this.emailService.sendEmail(subject, message, to);
    }
    public int OTPRequestThroughNumber(String phoneNumber, String name){
        String number = "+91"+phoneNumber;
        Random rand = new Random();
        int otpCheck = rand.nextInt(899999) + 100000;
        sendSMS(number, name, otpCheck);
        return otpCheck;
    }
    @Async
    public void sendSMS(String number, String name, int otpCheck){
        try {
            String myMessage = "Dear " + name + "," +
                    "\nThe One Time Password (OTP) to verify your mobile number is " + otpCheck +
                    "\nThe One Time Password is valid for the next 10 minutes." +
                    "\n(This is an auto generated sms, so please do not reply back.)" +
                    "\nRegards," +
                    "\nTeam HeathHive" +
                    "\nhealthiveforall@gmail.com";
            Message message = Message.creator(new com.twilio.type.PhoneNumber(number), new com.twilio.type.PhoneNumber(twilioConfig.getFromNumber()), myMessage).create();
            System.out.println(message);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}