package dev.mstefanov.hms.service.impl;

import dev.mstefanov.hms.model.Status;
import dev.mstefanov.hms.model.service.StatusServiceModel;
import dev.mstefanov.hms.repository.StatusRepository;
import dev.mstefanov.hms.service.StatusService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public Status getRequestedStatus () {
        return this.statusRepository.findStatusByName("REQUESTED");
    }

    @Override
    public Status getConfirmedStatus() {
        return this.statusRepository.findStatusByName("CONFIRMED");
    }

    @Override
    public Status getArchivedStatus() {
        return this.statusRepository.findStatusByName("ARCHIVED");
    }

    @Override
    public List<StatusServiceModel> getAllStatuses() {

        List <StatusServiceModel> statuses = new ArrayList<>();

        for (Status status : this.statusRepository.findAll()) {
            statuses.add(this.modelMapper.map(status, StatusServiceModel.class));
        }

        return statuses;
    }
}