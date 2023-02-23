package com.healthive.Service.Impl;
import com.healthive.Config.UserCache;
import com.healthive.Models.User;
import com.healthive.Payloads.*;
import com.healthive.Repository.UserRepo;
import com.healthive.Security.JwtTokenHelper;
import com.healthive.Service.OTPService;
import com.healthive.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import java.util.Objects;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.OK;
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final ModelMapper modelMapper;
    private final OTPService otpService;
    private final JwtTokenHelper jwtTokenHelper;
    private final UserCache userCache;
    private final UserRepo userRepo;
    private final UserDetailsService userDetailsService;
    @Override
    public ResponseEntity<?> updateUserProfile(User user, UserProfile userProfile) {
        if (userProfile.getGender().equals("f")) {
            user.setGender("female");
        } else {
            user.setGender("male");
        }
        user.setFirstname(userProfile.getFirstname());
        user.setLastname(userProfile.getLastname());
        this.userRepo.save(user);
        userProfile = this.modelMapper.map(user, UserProfile.class);
        return new ResponseEntity<>(userProfile, OK);
    }
    @Override
    public ResponseEntity<?> sendPhoneOTP(User user, TwilioCacheDto twilioCacheDto) {
        String email = user.getEmail();
        if (this.userRepo.existsUserByPhoneNumber(twilioCacheDto.getPhoneNumber())) {
            return new ResponseEntity<>(new ApiResponse("Phone Number already linked with other account", false), NOT_ACCEPTABLE);
        }
        twilioCacheDto.setOne_time_password(this.otpService.OTPRequestThroughNumber(twilioCacheDto.getPhoneNumber(), user.getFirstname()));
        if (this.userCache.isCachePresent(email)) {
            this.userCache.clearCache(email);
        }
        this.userCache.setUserCache(email, twilioCacheDto);
        return new ResponseEntity<>(new ApiResponse("OTP has been successfully generated", true), OK);
    }
    @Override
    public ResponseEntity<?> verifyResetPhoneOTP(User user, TwilioCacheDto twilioCacheDto){
        if (!this.userCache.isCachePresent(user.getEmail())) {
            return new ResponseEntity<>(new ApiResponse("Invalid Request", false), HttpStatus.FORBIDDEN);
        }
        TwilioCacheDto storedOtpDto = (TwilioCacheDto)this.userCache.getCache(user.getEmail());
        if (Objects.equals(storedOtpDto.getOne_time_password(), twilioCacheDto.getOne_time_password())) {
            this.userCache.clearCache(user.getEmail());
            user.setPhoneNumber(twilioCacheDto.getPhoneNumber());
            this.userRepo.save(user);
            return new ResponseEntity<>(new ApiResponse("OTP successfully verified, phone number has been updated", true), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ApiResponse("Invalid OTP or Action not required!!", false), HttpStatus.NOT_ACCEPTABLE);
        }
    }
    @Override
    public ResponseEntity<?> sendEmailOTP(User user, String email) throws Exception {
        if(this.userRepo.findByEmail(email).isPresent()){
            return new ResponseEntity<>(new ApiResponse("Entered email already linked with other account", false), NOT_ACCEPTABLE);
        }
        try {
            if (this.userCache.isCachePresent(email)) {
                this.userCache.clearCache(email);
            }
            OtpDto otpDto = new OtpDto(email, this.otpService.OTPRequest(email), null, false);
            this.userCache.setUserCache(email, otpDto);
            return new ResponseEntity<>(new ApiResponse("OTP Sent Success on the entered Email", true), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new ApiResponse("Can't able to make your request", false), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
    @Override
    public ResponseEntity<?> verifyResetEmailOTP(User user, OtpDto otpDto){
        if (!this.userCache.isCachePresent(otpDto.getEmail())) {
            return new ResponseEntity<>(new ApiResponse("Invalid Request", false), HttpStatus.FORBIDDEN);
        }
        OtpDto storedOtpDto = (OtpDto)this.userCache.getCache(otpDto.getEmail());
        if (Objects.equals(storedOtpDto.getOne_time_password(), otpDto.getOne_time_password())) {
            this.userCache.clearCache(otpDto.getEmail());
            user.setEmail(otpDto.getEmail());
            this.userRepo.saveAndFlush(user);
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(otpDto.getEmail());
            JwtAuthResponse response = new JwtAuthResponse(
                    this.jwtTokenHelper.generateAccessToken(userDetails),
                    this.jwtTokenHelper.generateRefreshToken(userDetails),
                    user.getFirstname(),
                    user.getLastname(),
                    otpDto.getEmail(),
                    user.getPrivateKey(),
                    user.getRoles()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse("Invalid OTP or Action not required!!", false), HttpStatus.NOT_ACCEPTABLE);
    }
}
