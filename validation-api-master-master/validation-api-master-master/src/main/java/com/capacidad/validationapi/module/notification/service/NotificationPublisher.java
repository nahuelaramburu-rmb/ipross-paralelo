package com.capacidad.validationapi.module.notification.service;

import com.capacidad.validationapi.module.notification.model.Notification;

import java.io.File;
import java.util.List;

public interface NotificationPublisher {

    void publishToResourceId(Notification notification, List<String> resourceIds);

    void publishToSub(Notification notification, List<String> subs);

    void publishToRole(Notification notification, List<String> roles);

    void publishToTopic(File file);

}
