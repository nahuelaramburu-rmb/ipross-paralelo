package com.capacidad.validationapi.config;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class SqlFunctionsMetadataBuilderContributor implements MetadataBuilderContributor {
    @Override
    public void contribute(MetadataBuilder metadataBuilder) {
        metadataBuilder.applySqlFunction(
                "date_tz",
                new SQLFunctionTemplate(
                        StandardBasicTypes.TIMESTAMP,
                        "date(?1 at time zone ?2)"
                )
        );
    }
}
