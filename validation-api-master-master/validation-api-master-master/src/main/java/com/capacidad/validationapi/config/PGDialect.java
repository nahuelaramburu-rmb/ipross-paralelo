package com.capacidad.validationapi.config;

import org.hibernate.dialect.PostgreSQL10Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class PGDialect extends PostgreSQL10Dialect {

    public PGDialect() {
        super();
        super.registerFunction("string_agg", new SQLFunctionTemplate(StandardBasicTypes.STRING, "string_agg(?1, ?2)"));
        super.registerFunction("age_from_birthdate",
                new SQLFunctionTemplate(StandardBasicTypes.DATE,
                        "date_part('year',age(?1))"));
    }

}
