package com.forstudy.pc.communicator.Services;

import org.springframework.http.ResponseEntity;

/**
 * Created by pc on 05.03.17.
 */

public interface ServiceCallbacks {
    void taskResponseCall(ResponseEntity<?> response, int whatTask);
}

