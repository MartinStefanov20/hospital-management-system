package com.example.proekt.messages;

public class ValidationErrorMessages {

    public static final String INCORRECT_USERNAME_LENGTH = "Please note that the username should contain between 4 and 30 characters.";
    public static final String INCORRECT_PASSWORD_LENGTH = "Please note that the password should contain between 4 and 16 symbols.";
    public static final String INCORRECT_FIRST_NAME_LENGTH = "Please note that your first name should contain between 3 and 20 characters.";
    public static final String INCORRECT_LAST_NAME_LENGTH = "Please note that your last name should contain between 3 and 20 characters.";
    public static final String INVALID_ROLE = "Invalid user role.";
    public static final String INVALID_PRESCRIPTION_MESSAGE = "Please note that prescription text should contain between 3 and 2000 characters.";
    public static final String INCORRECT_SALUTATION = "Please select a correct salutation from the drop-down menu.";
    public static final String INCORRECT_BIRTHDAY = "Please note that your birthday should be in the past.";
    public static final String INVALID_STATUS = "Invalid appointment status. Please note the available statuses are \"REQUESTED\", \"CONFIRMED\", \"ARCHIVED\"";
    public static final String INCORRECT_PRESCRIPTION_DATE = "Prescription date cannot be in the past";
    public static final String APPOINTMENT_DATE_MANDATORY = "Please note that setting appointment date is mandatory upon confirmation";
    public static final String INVALID_APPOINTMENT_ID = "Please note that appointment id is mandatory upon issuing a prescription.";
    public static final String INVALID_PATIENT_ID = "Please note that patient id is mandatory upon issuing a prescription.";
    public static final String NOTES_CANNOT_BE_NULL = "Please note that notes are mandatory upon issuing a prescription.";
    public static final String INVALID_NOTES_SIZE = "Please note that notes should contain between 10 and 2000 characters.";
    public static final String DOCTOR_USERNAME_MISSING = "Please note that doctor username is mandatory upon issuing a prescription.";
    public static final String PRESCRIPTION_ID_MANDATORY = "Please note that prescription id is mandatory upon editing a prescription.";
    public static final String MISSING_USERNAME = "Please note that username is mandatory when editing user`s roles.";
    public static final String MISSING_BIRTHDAY = "Please note that birth date is mandatory field.";

}
