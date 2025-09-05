package com.capacidad.validationapi.module.importprocessor.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.importprocessor.model.ImportReport;
import com.capacidad.validationapi.module.importprocessor.model.OperationResolverProperties;

import java.io.Serializable;

public interface ImportOperationResolver<T extends BaseEntity<I>, I extends Serializable> {

    ImportReport executeImportOperation(OperationResolverProperties<T> operationResolverProperties) throws ObjectNotValidException, ObjectNotFoundException;

}
