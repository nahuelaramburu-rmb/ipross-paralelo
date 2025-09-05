package com.capacidad.validationapi.module.notification.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryFinder;
import com.capacidad.validationapi.module.notification.dto.BeneficiaryNotificationDTO;
import com.capacidad.validationapi.module.notification.model.Notification;
import com.capacidad.validationapi.module.notification.model.NotificationType;
import com.capacidad.validationapi.module.notification.service.NotificationPublisher;
import com.capacidad.validationapi.module.notification.service.NotificationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final BeneficiaryFinder beneficiaryFinder;
    private final NotificationPublisher notificationPublisher;

    @Autowired
    public NotificationServiceImpl(BeneficiaryFinder beneficiaryFinder,
                                   NotificationPublisher notificationPublisher) {
        this.beneficiaryFinder = beneficiaryFinder;
        this.notificationPublisher = notificationPublisher;
    }

    @Override
    public Notification sendNotificationToBeneficiaryFamilyGroup(BeneficiaryNotificationDTO input) throws ObjectNotFoundException, ObjectNotValidException {
        var resourceIdList = buildBeneficiariesResourceIdList(input);
        var notification = new Notification(NotificationType.ALL,
                TenantContext.getTenant(),
                input.getTitle(),
                input.getBody(),
                buildExtraData(input));
        notificationPublisher.publishToResourceId(notification, resourceIdList.stream()
                .map(UUID::toString)
                .collect(Collectors.toList()));
        return notification;
    }

    private List<UUID> buildBeneficiariesResourceIdList(BeneficiaryNotificationDTO input) throws ObjectNotFoundException {
        var resourceIds = new ArrayList<UUID>();
        var beneficiary = beneficiaryFinder.findBeneficiary(input.getBeneficiaryCode());
        if (input.isNotifyFamilyGroup())
            resourceIds.addAll(beneficiaryFinder.getFamily(beneficiary.getFamilyId()).stream()
                    .map(Beneficiary::getResourceId)
                    .collect(Collectors.toList()));
        else
            resourceIds.add(beneficiary.getResourceId());
        return resourceIds;
    }

    private Map<String, Object> buildExtraData(BeneficiaryNotificationDTO input) throws ObjectNotValidException {
        if (input.getExtraData() != null) {
            var extraData = input.getExtraData();
            if (!extraData.containsKey("type") || !(extraData.get("type") instanceof String)
                    || StringUtils.isEmpty((CharSequence) extraData.get("type")))
                throw new ObjectNotValidException("notification.invalidType");
            return extraData;
        }
        return Collections.emptyMap();
    }

}
