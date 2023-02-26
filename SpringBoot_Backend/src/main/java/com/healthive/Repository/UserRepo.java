package com.healthive.Repository;
import com.healthive.Models.PatientProfile;
import com.healthive.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    Boolean existsUserByPhoneNumber(String phoneNumber);
    User findByProfile(PatientProfile profile);
}
