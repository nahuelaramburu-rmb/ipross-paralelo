package com.capacidad.validationapi.module.beneficiary.service.impl;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.misc.LocaleHandler;
import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.beneficiary.service.BeneficiaryService;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.exportprocessor.service.FileDownloadKeyService;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BeneficiaryExportServiceImplTest {

    @Mock
    private LocaleHandler localeHandler;

    @Mock
    private FileDownloadKeyService fileDownloadKeyService;

    @Mock
    private BeneficiaryService beneficiaryService;

    @Mock
    private EntityManager processingEntityManager;

    @Mock
    private HikariDataSourcePoolMetadata dataSourcePoolMetadata;

    @Spy
    @InjectMocks
    private BeneficiaryExportServiceImpl beneficiaryExportService;

    @Before
    public void initialize() {
        CriteriaBuilder criteriaBuilder = mock(CriteriaBuilder.class);
        CriteriaQuery<Tuple> tupleCriteriaQuery = mock(CriteriaQuery.class);
        TypedQuery<Tuple> tupleTypedQuery = mock(TypedQuery.class);
        Root<Beneficiary> root = mock(Root.class);
        when(localeHandler.getLocaleMessage(anyString(), any(Locale.class))).thenReturn(Optional.empty());
        when(beneficiaryExportService.getProcessingDataSourcePoolMetadata()).thenReturn(dataSourcePoolMetadata);
        when(dataSourcePoolMetadata.getIdle()).thenReturn(2);
        when(beneficiaryExportService.getProcessingEntityManager()).thenReturn(processingEntityManager);
        when(processingEntityManager.createQuery(tupleCriteriaQuery)).thenReturn(tupleTypedQuery);
        when(processingEntityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createTupleQuery()).thenReturn(tupleCriteriaQuery);
        when(tupleCriteriaQuery.from(Beneficiary.class)).thenReturn(root);
        doReturn(Collections.emptyList()).when(beneficiaryExportService).buildSelections(root, tupleCriteriaQuery, criteriaBuilder);
        doReturn(Sort.unsorted()).when(beneficiaryExportService).getSort();
        when(beneficiaryExportService.getFileDownloadKeyService()).thenReturn(fileDownloadKeyService);
        beneficiaryExportService.initialize();
    }

    @Test
    public void testExportStreamToCsvDoesNothingWhenInvalidKey() throws ObjectNotValidException, ObjectNotFoundException {
        when(fileDownloadKeyService.findDownloadKeyTypedQuery(any(), any(), any())).thenThrow(new ObjectNotFoundException(""));

        beneficiaryExportService.exportStreamToCsv("search", "key", mock(OutputStream.class));

        verify(beneficiaryService, never()).findAll(any(Specification.class), anyMap(), any(Sort.class));
    }

    @Test
    public void testExportStreamToCsvExecutesWhenValidKeyEmptySearch() throws ObjectNotValidException, ObjectNotFoundException, IOException {
        Authentication authentication = mock(Authentication.class);
        OutputStream outputStream = mock(OutputStream.class);

        FileDownloadKey fileDownloadKey = new FileDownloadKey();
        fileDownloadKey.setTenantId(UUID.randomUUID());
        fileDownloadKey.setSerializedAuthentication(authentication);

        Session sessionMock = mock(Session.class);

        when(fileDownloadKeyService.findDownloadKeyTypedQuery(any(), any(), any())).thenReturn(fileDownloadKey);
        when(beneficiaryService.buildSpecification("")).thenReturn(Optional.empty());
        when(processingEntityManager.unwrap(Session.class)).thenReturn(sessionMock);
        when(sessionMock.doReturningWork(any(ReturningWork.class))).thenReturn(true);

        beneficiaryExportService.exportStreamToCsv("", "key", outputStream);

        verify(outputStream, times(1)).write(any(byte[].class), anyInt(), anyInt());
    }

    @Test
    public void testExportStreamToCsvExecutesWhenValidKeyValidSearch() throws ObjectNotValidException, ObjectNotFoundException, IOException {
        Authentication authentication = mock(Authentication.class);
        OutputStream outputStream = mock(OutputStream.class);

        FileDownloadKey fileDownloadKey = new FileDownloadKey();
        fileDownloadKey.setTenantId(UUID.randomUUID());
        fileDownloadKey.setSerializedAuthentication(authentication);

        String search = "search";

        Session sessionMock = mock(Session.class);

        when(fileDownloadKeyService.findDownloadKeyTypedQuery(any(), any(), any())).thenReturn(fileDownloadKey);
        when(processingEntityManager.unwrap(Session.class)).thenReturn(sessionMock);
        when(sessionMock.doReturningWork(any(ReturningWork.class))).thenReturn(true);

        beneficiaryExportService.exportStreamToCsv(search, "key", outputStream);

        verify(outputStream, times(1)).write(any(byte[].class), anyInt(), anyInt());
    }

}
