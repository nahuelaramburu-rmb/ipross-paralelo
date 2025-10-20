package com.capacidad.validationapi.module.exportprocessor.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.exportprocessor.repository.FileDownloadKeyRepository;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class FileDownloadKeyServiceImpl implements FileDownloadKeyService {

    private final FileDownloadKeyRepository fileDownloadKeyRepository;
    private final Hashids hashids;

    @Autowired
    public FileDownloadKeyServiceImpl(FileDownloadKeyRepository fileDownloadKeyRepository,
                                      Hashids hashids) {
        this.fileDownloadKeyRepository = fileDownloadKeyRepository;
        this.hashids = hashids;
    }

    @Override
    public FileDownloadKey generateDownloadKey(String origin) {
        String key = hashids.encode(Instant.now().toEpochMilli());
        FileDownloadKey fileDownloadKey = new FileDownloadKey();
        fileDownloadKey.setKey(key);
        fileDownloadKey.setOrigin(origin);
        fileDownloadKey.setSerializedAuthentication(SecurityContextHolder.getContext().getAuthentication());
        return fileDownloadKeyRepository.save(fileDownloadKey);
    }

    @Override
    public FileDownloadKey findDownloadKeyTypedQuery(String key, String origin, EntityManager entityManager) throws ObjectNotFoundException, ObjectNotValidException {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<FileDownloadKey> criteriaQuery = criteriaBuilder.createQuery(FileDownloadKey.class);
        Root<FileDownloadKey> root = criteriaQuery.from(FileDownloadKey.class);
        criteriaQuery.select(root);
        criteriaQuery.where(criteriaBuilder.and(
                criteriaBuilder.equal(root.get("deleted"), false),
                criteriaBuilder.like(root.get("key"), key),
                criteriaBuilder.like(root.get("origin"), origin)));
        TypedQuery<FileDownloadKey> typedQuery = entityManager.createQuery(criteriaQuery);
        return getSingleResultAndValidate(typedQuery);
    }

    private FileDownloadKey getSingleResultAndValidate(TypedQuery<FileDownloadKey> typedQuery) throws ObjectNotFoundException, ObjectNotValidException {
        try {
            FileDownloadKey result = typedQuery.getSingleResult();
            if (result.getCreatedAt().plusMinutes(1).isBefore(LocalDateTime.now()))
                throw new ObjectNotValidException("fileDownloadKey.expiredKey");
            return result;
        } catch (NoResultException e) {
            throw new ObjectNotFoundException("fileDownloadKey.keyNotFound");
        }
    }

    @Override
    public void deleteAllDownloadKeys() {
        fileDownloadKeyRepository.deleteAll();
    }
}
