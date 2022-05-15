package com.example.proekt.utils.roleValidator;

import javax.validation.Payload;

public @interface ValidateRole {

    String[] acceptedValues();

    String message() default "{uk.dds.ideskos.validator.ValidateString.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
