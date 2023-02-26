package com.healthive.Service;
import com.healthive.Models.User;
import com.healthive.Payloads.OtpDto;
import com.healthive.Payloads.TwilioCacheDto;
import com.healthive.Payloads.UserProfile;
import org.springframework.http.ResponseEntity;
public interface UserService {
    ResponseEntity<?> updateUserProfile(User user, UserProfile userProfile);

    ResponseEntity<?> sendPhoneOTP(User user, TwilioCacheDto twilioCacheDto);

    ResponseEntity<?> verifyResetPhoneOTP(User user, TwilioCacheDto twilioCacheDto);

    ResponseEntity<?> sendEmailOTP(User user, String email) throws Exception;

    ResponseEntity<?> verifyResetEmailOTP(User user, OtpDto otpDto);
}
