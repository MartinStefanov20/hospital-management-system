package dev.mstefanov.hms.service;

import dev.mstefanov.hms.model.Status;
import dev.mstefanov.hms.model.service.StatusServiceModel;

import java.util.List;

public interface StatusService {

    Status getRequestedStatus ();

    Status getConfirmedStatus ();

    List<StatusServiceModel> getAllStatuses();

    Status getArchivedStatus();

}
