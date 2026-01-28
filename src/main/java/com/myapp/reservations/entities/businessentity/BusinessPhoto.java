package com.myapp.reservations.entities.businessentity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "business_photos")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class BusinessPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "photo_id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "caption", length = 255)
    private String caption;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.displayOrder == null) {
            this.displayOrder = 0;
        }
    }
}
