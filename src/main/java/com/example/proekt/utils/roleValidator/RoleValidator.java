package com.example.proekt.utils.roleValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;

public class RoleValidator implements ConstraintValidator<ValidateRole, String>{

    private List<String> valueList;

    @Override
    public void initialize(ValidateRole constraintAnnotation) {

        this.valueList = new ArrayList<String>();

        for(String val : constraintAnnotation.acceptedValues()) {
            valueList.add(val.toUpperCase());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        return valueList.contains(value.toUpperCase());
    }
}
