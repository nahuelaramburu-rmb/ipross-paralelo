package com.capacidad.validationapi.module.person.model;

import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import com.capacidad.validationapi.module.location.model.Address;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Locale;

@MappedSuperclass
@NoArgsConstructor
@Getter
@Setter
public class Person extends BaseTenantEntity<Long> {

    @Audited(withModifiedFlag = true)
    @Column(nullable = false)
    private String name;

    @Audited(withModifiedFlag = true)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Audited(withModifiedFlag = true)
    @Column(name = "id_number", nullable = false)
    private Long idNumber;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_id", nullable = false)
    private IdType idType;

    @Audited(withModifiedFlag = true)
    @Column(name = "work_id_number")
    private Long workIdNumber;

    @Audited(withModifiedFlag = true)
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Audited(withModifiedFlag = true)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marital_status_id")
    private MaritalStatus maritalStatus;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "occupation_id")
    private Occupation occupation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "studies_id")
    private Studies studies;

    @Audited(withModifiedFlag = true, targetAuditMode = RelationTargetAuditMode.AUDITED)
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;


    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "phone_id")
    private Phone phone;

    @Column
    private String email;

    public static String[] getHeaderNames(LocaleHandler localeHandler, Locale locale) {
        String[] stringHeaders = new String[14];
        stringHeaders[0] = localeHandler.getLocaleMessage("fields.name", locale).orElse("");
        stringHeaders[1] = localeHandler.getLocaleMessage("fields.idType", locale).orElse("");
        stringHeaders[2] = localeHandler.getLocaleMessage("fields.idNumber", locale).orElse("");
        stringHeaders[3] = localeHandler.getLocaleMessage("fields.workIdNumber", locale).orElse("");
        stringHeaders[4] = localeHandler.getLocaleMessage("fields.gender", locale).orElse("");
        stringHeaders[5] = localeHandler.getLocaleMessage("fields.birthDate", locale).orElse("");
        stringHeaders[6] = localeHandler.getLocaleMessage("fields.city", locale).orElse("");
        stringHeaders[7] = localeHandler.getLocaleMessage("fields.address", locale).orElse("");
        stringHeaders[8] = localeHandler.getLocaleMessage("fields.maritalStatus", locale).orElse("");
        stringHeaders[9] = localeHandler.getLocaleMessage("fields.occupation", locale).orElse("");
        stringHeaders[10] = localeHandler.getLocaleMessage("fields.studies", locale).orElse("");
        stringHeaders[11] = localeHandler.getLocaleMessage("fields.phone", locale).orElse("");
        stringHeaders[12] = localeHandler.getLocaleMessage("fields.email", locale).orElse("");
        stringHeaders[13] = localeHandler.getLocaleMessage("fields.createdAt", locale).orElse("");
        return stringHeaders;
    }

}
