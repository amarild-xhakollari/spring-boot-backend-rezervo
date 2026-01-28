package com.myapp.reservations.service;

import com.myapp.reservations.dto.timeoffdto.offeringdto.OfferingRequest;
import com.myapp.reservations.dto.timeoffdto.offeringdto.OfferingResponse;
import com.myapp.reservations.mapper.OfferingMapper;
import com.myapp.reservations.repository.BusinessRepository;
import com.myapp.reservations.repository.OfferingRepository;
import com.myapp.reservations.entities.businessentity.Business;
import com.myapp.reservations.entities.BusinessSchedule.Offering;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OfferingService {

    private final OfferingRepository offeringRepository;
    private final BusinessRepository businessRepository;

    public OfferingService(OfferingRepository offeringRepository, BusinessRepository businessRepository) {
        this.offeringRepository = offeringRepository;
        this.businessRepository = businessRepository;
    }

    @Transactional
    public OfferingResponse createService(@NotNull UUID businessId, @NotNull OfferingRequest offeringRequest) {
        Business business = businessRepository.findById(businessId).orElseThrow();

        Offering offering = OfferingMapper.toOffering(offeringRequest);
        offering.setBusiness(business);

        offeringRepository.save(offering);


        return OfferingMapper.toResponse(offeringRepository.save(offering));
    }

    public OfferingResponse getOfferingById(UUID offeringId){

        return OfferingMapper.toResponse(offeringRepository.getOfferingById(offeringId));
    }

    public List<OfferingResponse> getBusinessOfferings(UUID businessId){
        if(businessId == null){
            return null;
        }
        Business business = businessRepository.getBusinessById(businessId).orElseThrow(()-> new RuntimeException("Business not found"));
        return business.getOfferings().stream().map(OfferingMapper::toResponse).toList();
    }

    public void deleteOfferingId(UUID offeringId){
        offeringRepository.deleteById(offeringId);
    }

    @Transactional
    public OfferingResponse updateOffering(UUID offeringId, OfferingRequest request) {
        Offering existing = offeringRepository.findById(offeringId)
                .orElseThrow(() -> new RuntimeException("Offering not found"));

        if (request.name() != null) existing.setName(request.name());
        if (request.description() != null) existing.setDescription(request.description());
        if (request.price() != null) existing.setPrice(request.price());
        if (request.durationMinutes() != null) existing.setDurationMinutes(request.durationMinutes());
        if (request.bufferTimeMinutes() != null) existing.setBufferTimeMinutes(request.bufferTimeMinutes());

        Offering saved = offeringRepository.save(existing);
        return OfferingMapper.toResponse(saved);
    }



}
