package com.myapp.reservations.repository;

import com.myapp.reservations.entities.businessentity.BusinessPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BusinessPhotoRepository extends JpaRepository<BusinessPhoto, UUID> {

    List<BusinessPhoto> findByBusinessIdOrderByDisplayOrderAscCreatedAtDesc(UUID businessId);

    @Query("SELECT COALESCE(MAX(p.displayOrder), 0) FROM BusinessPhoto p WHERE p.business.id = :businessId")
    Integer findMaxDisplayOrderByBusinessId(UUID businessId);

    long countByBusinessId(UUID businessId);
}
