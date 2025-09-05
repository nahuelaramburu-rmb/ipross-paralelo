package com.capacidad.validationapi.module.encryption;

import com.capacidad.utils.exception.ObjectNotValidException;

public interface EncryptionService {

    String decrypt(String content) throws ObjectNotValidException;

    String encrypt(String content) throws ObjectNotValidException;

}
