package com.myapp.reservations.exception.conflictexceptions;

import com.myapp.reservations.exception.ConflictException;

public class MaxPhotosReachedException extends ConflictException {
    public MaxPhotosReachedException(int maxPhotos){

        super("Maximum number of photos (" + maxPhotos + ") reached");
    }

    public MaxPhotosReachedException(){

        super("Maximum number of photos reached");
    }
}
