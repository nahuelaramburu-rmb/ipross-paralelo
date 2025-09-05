package com.capacidad.validationapi.module.storage.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.exportprocessor.service.FileDownloadKeyServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileDownloadKeyServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<FileDownloadKey> typedQuery;

    @InjectMocks
    private FileDownloadKeyServiceImpl fileDownloadKeyService;

    @Before
    public void init() {
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery<FileDownloadKey> criteriaQuery = mock(CriteriaQuery.class);
        Root<FileDownloadKey> root = mock(Root.class);
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(FileDownloadKey.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(FileDownloadKey.class)).thenReturn(root);
        when(entityManager.createQuery(criteriaQuery)).thenReturn(typedQuery);
    }

    @Test
    public void testFindDownloadKeyFailsWhenNotFound() {
        String key = "key";
        String origin = "origin";

        when(typedQuery.getSingleResult()).thenThrow(new NoResultException(""));

        ObjectNotFoundException exception = (ObjectNotFoundException) catchThrowable(() -> fileDownloadKeyService.findDownloadKeyTypedQuery(key, origin, entityManager));

        assertThat(exception.getMessage()).isEqualTo("fileDownloadKey.keyNotFound");
    }

    @Test
    public void testFindDownloadKeyFailsWhenKeyExpired() {
        String key = "key";
        String origin = "origin";

        FileDownloadKey fileDownloadKey = new FileDownloadKey();
        fileDownloadKey.setCreatedAt(LocalDateTime.now().minusMinutes(1));

        when(typedQuery.getSingleResult()).thenReturn(fileDownloadKey);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> fileDownloadKeyService.findDownloadKeyTypedQuery(key, origin, entityManager));

        assertThat(exception.getMessage()).isEqualTo("fileDownloadKey.expiredKey");
    }

    @Test
    public void testFindDownloadKeyDoNotFailsWhenValidKey() throws ObjectNotValidException, ObjectNotFoundException {
        String key = "key";
        String origin = "origin";

        FileDownloadKey fileDownloadKey = new FileDownloadKey();
        fileDownloadKey.setCreatedAt(LocalDateTime.now());

        when(typedQuery.getSingleResult()).thenReturn(fileDownloadKey);

        FileDownloadKey result = fileDownloadKeyService.findDownloadKeyTypedQuery(key, origin, entityManager);

        assertThat(result).isEqualTo(fileDownloadKey);
    }

}
