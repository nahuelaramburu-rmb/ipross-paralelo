package com.capacidad.validationapi.module.exportprocessor.model;

import com.capacidad.validationapi.module.base.model.BaseTenantEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.Authentication;

import javax.persistence.*;

@Entity
@Table(name = "file_download_key",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"download_key", "origin", "deleted", "deletion_token", "tenant_id"}),
        })
@NoArgsConstructor
@Getter
@Setter
@SequenceGenerator(name = "base_seq_gen", sequenceName = "file_download_key_seq")
public class FileDownloadKey extends BaseTenantEntity<Long> {

    @Column(name = "download_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private String origin;

    @Column(name = "serialized_authentication", nullable = false)
    private Authentication serializedAuthentication;

}
