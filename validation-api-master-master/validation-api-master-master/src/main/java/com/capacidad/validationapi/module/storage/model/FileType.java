package com.capacidad.validationapi.module.storage.model;

public enum FileType {
    SIGNATURE("medical-authorizations", "signatures"),
    REPORT("medical-authorizations", "reports"),
    ATTACHMENT("medical-authorizations", "attachments"),
    BENEFICIARY_PROFILE("beneficiaries", "profile"),
    PRACTITIONER_PROFILE("practitioners", "profile"),
    BENEFICIARY_PROCEDURE("beneficiaries", "procedures"),
    BATCH_ATTACHMENT("batches1", "attachments");

    private final String folder;
    private final String container;


    FileType(String container, String folder) {
        this.container = container;
        this.folder = folder;
    }

    public String getFolder() {
        return this.folder;
    }

    public String getContainer() {
        return this.container;
    }
}
