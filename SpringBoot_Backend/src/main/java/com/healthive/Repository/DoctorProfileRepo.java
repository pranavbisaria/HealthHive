package com.healthive.Repository;
import com.healthive.Models.DoctorProfile;
import com.healthive.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface DoctorProfileRepo extends JpaRepository <DoctorProfile, Long> {
    DoctorProfile findByUser(User user);
}
