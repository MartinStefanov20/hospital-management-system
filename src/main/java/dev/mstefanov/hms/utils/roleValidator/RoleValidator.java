package dev.mstefanov.hms.utils.roleValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.ArrayList;
import java.util.List;

public class RoleValidator implements ConstraintValidator<ValidateRole, String> {

    private List<String> valueList;

    @Override
    public void initialize(ValidateRole constraintAnnotation) {

        this.valueList = new ArrayList<>();

        for (String val : constraintAnnotation.acceptedValues()) {
            valueList.add(val.toUpperCase());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null) {
            return false;
        }

        return valueList.contains(value.toUpperCase());
    }
}
