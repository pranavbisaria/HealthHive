package com.healthive.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@Getter@Setter
@AllArgsConstructor
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private String firstname;
    private String lastname;
    @Column(nullable = false, unique = true)
    private String email;
    private String password;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Post> likedPost;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Comments> likedComments;
    private String gender;
    private String privateKey = UUID.randomUUID().toString();
    private BigInteger serial = new BigInteger("-1");
    @Column(unique = true)
    private String phoneNumber;
    @Column(length = 1000)
    private String profilePhoto;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PatientProfile profile = null;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DoctorProfile doctorProfile = null;
    private Boolean active = true;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user", referencedColumnName = "id"), inverseJoinColumns =  @JoinColumn(name = "role", referencedColumnName = "id"))
    private Set<Role> roles = new HashSet<>(0);
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map((role)-> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }
    @Override
    public String getUsername() {
        return this.email;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return this.active;
    }
    public void setEnable(boolean status) {
        this.active = status;
    }
}