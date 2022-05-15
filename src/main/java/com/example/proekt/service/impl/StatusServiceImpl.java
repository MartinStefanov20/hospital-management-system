package com.example.proekt.service.impl;

import com.example.proekt.model.Status;
import com.example.proekt.model.service.StatusServiceModel;
import com.example.proekt.repository.StatusRepository;
import com.example.proekt.service.StatusService;
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
    public void initStatuses(){

        if (statusRepository.count() == 0) {

            Status requested = new Status("REQUESTED", "Your appointment has been " +
                    "requested. Up to 24 hours after the request our team will contact you to arrange the details.");

            Status confirmed = new Status("CONFIRMED", "Your appointment has been" +
                    " confirmed. Our specialist will be waiting for you at the confirmed day and time." +
                    "You could always refer to your Appointments section for details.");

            Status archived = new Status("ARCHIVED", "Your appointment has been archived.");

            this.statusRepository.saveAll(List.of(requested, confirmed, archived));
        }
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