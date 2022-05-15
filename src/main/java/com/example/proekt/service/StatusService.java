package com.example.proekt.service;

import com.example.proekt.model.Status;
import com.example.proekt.model.service.StatusServiceModel;

import java.util.List;

public interface StatusService {

    Status getRequestedStatus ();

    Status getConfirmedStatus ();

    List<StatusServiceModel> getAllStatuses();

    Status getArchivedStatus();

    void initStatuses();

}
