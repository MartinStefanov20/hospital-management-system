package dev.mstefanov.hms.service.impl;

import dev.mstefanov.hms.model.Status;
import dev.mstefanov.hms.model.service.StatusServiceModel;
import dev.mstefanov.hms.repository.StatusRepository;
import dev.mstefanov.hms.service.StatusService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatusServiceImpl implements StatusService {

    private final StatusRepository statusRepository;
    private final ModelMapper modelMapper;

    public StatusServiceImpl(StatusRepository statusRepository, ModelMapper modelMapper) {
        this.statusRepository = statusRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Status getRequestedStatus() {
        return status("REQUESTED");
    }

    @Override
    public Status getConfirmedStatus() {
        return status("CONFIRMED");
    }

    @Override
    public Status getArchivedStatus() {
        return status("ARCHIVED");
    }

    @Override
    public List<StatusServiceModel> getAllStatuses() {
        return statusRepository.findAll().stream()
                .map(status -> modelMapper.map(status, StatusServiceModel.class))
                .toList();
    }

    /** Statuses are reference data inserted by Flyway (V2); a missing one is a deployment error, not a 404. */
    private Status status(String name) {
        return statusRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Status " + name + " missing; has Flyway migration V2 run?"));
    }
}
