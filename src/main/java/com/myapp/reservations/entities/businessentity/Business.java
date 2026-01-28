package com.myapp.reservations.entities.businessentity;


import com.myapp.reservations.entities.BusinessSchedule.Offering;
import com.myapp.reservations.entities.BusinessSchedule.ScheduleSettings;
import com.myapp.reservations.entities.User.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "businesses")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Business {

    @Id
    @GeneratedValue(strategy =GenerationType.UUID)
    @Column(name = "b_id",unique = true, nullable = false)
    private UUID id;

    @Column(name = "b_name" ,length = 100,unique = true)
    private String name;

    @Column(name = "b_description" , length = 1000)
    private String description;

    @Column(name = "b_address" )
    private String address;

    @Column(name = "b_phone")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type")
    private BusinessType businessType;

    @Column(name = "custom_type")
    private String customType;

    @Column(name = "image_path")
    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "b_owner_id", nullable = false)
    private User owner;

    @ManyToMany(mappedBy = "adminOfBusinesses")
    private List<User> admins = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(
            name = "schedule_settings_id",
            referencedColumnName = "id"
    )
    private ScheduleSettings scheduleSettings;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Offering> offerings = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC, createdAt DESC")
    private List<BusinessPhoto> photos = new ArrayList<>();

    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    public void addOffering(Offering offering) {
        this.offerings.add(offering);
        offering.setBusiness(this);
    }
}
