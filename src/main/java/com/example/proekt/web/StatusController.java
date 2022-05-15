package com.example.proekt.web;

import com.example.proekt.model.service.StatusServiceModel;
import com.example.proekt.model.view.StatusViewModel;
import com.example.proekt.service.StatusService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class StatusController {

    private final StatusService statusService;
    private final ModelMapper modelMapper;

    public StatusController(StatusService statusService, ModelMapper modelMapper) {
        this.statusService = statusService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/appointments/statuses")
    public String getStatuses (Model model) {

        List<StatusViewModel> statuses = new ArrayList<>();
        for (StatusServiceModel allStatus : this.statusService.getAllStatuses()) {
            statuses.add(this.modelMapper.map(allStatus, StatusViewModel.class));
        }

        model.addAttribute("statuses", statuses);

        return "statuses-info";
    }

}
