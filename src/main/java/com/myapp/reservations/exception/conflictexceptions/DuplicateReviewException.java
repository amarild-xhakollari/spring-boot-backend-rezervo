package com.myapp.reservations.exception.conflictexceptions;

import com.myapp.reservations.exception.ConflictException;

import java.util.UUID;

public class DuplicateReviewException extends ConflictException {
    public DuplicateReviewException(){

        super("You have already reviewed this business");
    }

    public DuplicateReviewException(UUID businessId){

        super("You have already reviewed business with ID : " + businessId);
    }

    public DuplicateReviewException(String businessName){

        super("You have already reviewed business : " + businessName);
    }
}
