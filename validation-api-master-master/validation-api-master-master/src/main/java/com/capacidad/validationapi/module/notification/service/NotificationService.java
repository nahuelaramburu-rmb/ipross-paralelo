package com.capacidad.validationapi.module.notification.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.notification.dto.BeneficiaryNotificationDTO;
import com.capacidad.validationapi.module.notification.model.Notification;

public interface NotificationService {

    Notification sendNotificationToBeneficiaryFamilyGroup(BeneficiaryNotificationDTO input) throws ObjectNotFoundException, ObjectNotValidException;

}
