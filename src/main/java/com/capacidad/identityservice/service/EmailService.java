package com.capacidad.identityservice.service;

public interface EmailService {

    void sendMimeEmail(String to, String subject, String mimeText, boolean html);

}
