package com.myapp.reservations.entities.User;


import com.myapp.reservations.entities.businessentity.Business;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class User {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id",unique = true, nullable = false)
    private UUID id;

    @Column(name = "user_name", nullable = false,unique = true)
    private String name;

    @Column(name = "user_email", nullable = false,unique = true)
    private String email;

    @Column(name = "user_password", nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "user_roles")
    private Set<String> roles = new HashSet<>();


    @Column(name = "user_phone")
    private String phone;

    @OneToMany(mappedBy = "owner")
    private List<Business> ownedBusinesses = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "b_admins",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "b_id")
    )
    private List<Business> adminOfBusinesses = new ArrayList<>();

    @Column(name = "avatar_path")
    private String avatarPath;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

}
