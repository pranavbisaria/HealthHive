package com.healthive.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DoctorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;
    @OneToOne(cascade = CascadeType.ALL)
    private User user;
}
