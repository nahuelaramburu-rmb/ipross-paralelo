package com.capacidad.validationapi.module.render.service;

import com.capacidad.utils.exception.ObjectNotValidException;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public interface RenderService {

    ByteArrayOutputStream renderPDF(String templateName, Map<String, Object> templateValues) throws ObjectNotValidException;

    String renderQrCode(String content);

}
