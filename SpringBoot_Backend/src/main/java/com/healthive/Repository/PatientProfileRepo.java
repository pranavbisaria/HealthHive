package com.healthive.Repository;
import com.healthive.Models.PatientProfile;
import com.healthive.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientProfileRepo extends JpaRepository<PatientProfile, Long> {
    PatientProfile findByUser(User user);
}
