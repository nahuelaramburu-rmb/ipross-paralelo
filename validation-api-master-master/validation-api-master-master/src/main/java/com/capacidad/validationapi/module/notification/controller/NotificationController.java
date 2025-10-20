package com.capacidad.validationapi.module.notification.controller;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.notification.dto.BeneficiaryNotificationDTO;
import com.capacidad.validationapi.module.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_NOTIFICATION_PUBLICATIONS;

@RestController
@RequestMapping(value = ENDPOINT_NOTIFICATION_PUBLICATIONS)
public class NotificationController {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Autowired
    public NotificationController(NotificationService notificationService,
                                  ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/beneficiaries")
    public ResponseEntity<Object> sendNotificationToBeneficiaryFamilyGroup(@Valid @RequestBody BeneficiaryNotificationDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        var notification = notificationService.sendNotificationToBeneficiaryFamilyGroup(input);
        var response = objectMapper.createObjectNode();
        response.put("messageId", notification.getMessageId().toString());
        return ResponseEntity.ok(response);
    }

}
