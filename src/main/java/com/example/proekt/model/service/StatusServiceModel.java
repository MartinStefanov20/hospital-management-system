package com.example.proekt.model.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StatusServiceModel extends BaseServiceModel {

    private String name;
    private String statusDescription;

}
