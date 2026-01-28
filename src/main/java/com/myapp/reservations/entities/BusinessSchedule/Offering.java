package com.myapp.reservations.entities.BusinessSchedule;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.myapp.reservations.entities.businessentity.Business;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "offerings")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Offering {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;


    @Column(nullable = false)
    private Integer durationMinutes;


    private Integer bufferTimeMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b_id", nullable = false)
    @JsonIgnore
    private Business business;
}

