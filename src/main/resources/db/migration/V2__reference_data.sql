-- Appointment statuses (reference data; formerly seeded by StatusServiceImpl.initStatuses at startup).

insert into status (name, status_description) values
    ('REQUESTED', 'Your appointment has been requested. Up to 24 hours after the request our team will contact you to arrange the details.'),
    ('CONFIRMED', 'Your appointment has been confirmed. Our specialist will be waiting for you at the confirmed day and time. You could always refer to your Appointments section for details.'),
    ('ARCHIVED',  'Your appointment has been archived.');
