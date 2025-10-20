package com.capacidad.validationapi.module.exportprocessor.service;

import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.capacidad.validationapi.config.multitenancy.TenantContext;
import com.capacidad.validationapi.module.base.model.BaseEntity;
import com.capacidad.validationapi.module.base.service.BaseServiceFinder;
import com.capacidad.validationapi.module.exportprocessor.model.FileDownloadKey;
import com.capacidad.validationapi.module.render.service.impl.CSVWriterWrapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.metadata.HikariDataSourcePoolMetadata;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hibernate.jpa.QueryHints.*;
import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

@Slf4j
public abstract class BaseExportService<T extends BaseEntity<I>, I extends Serializable> implements ExportService<T, I> {

    private final BaseServiceFinder<T, I> baseServiceFinder;
    private final Class<T> modelType;
    @Autowired
    private FileDownloadKeyService fileDownloadKeyService;
    @Autowired
    private HikariDataSourcePoolMetadata processingDataSourcePoolMetadata;
    @PersistenceContext(unitName = "processing")
    private EntityManager processingEntityManager;
    private String[] headerNames;
    private Sort sort;
    private String origin;

    @SuppressWarnings("unchecked")
    public BaseExportService(BaseServiceFinder<T, I> baseServiceFinder) {
        this.baseServiceFinder = baseServiceFinder;
        Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(getClass(), BaseExportService.class);
        assert generics != null;
        this.modelType = (Class<T>) generics[0];
    }

    protected void initialize(String origin,
                              String[] headerNames,
                              Sort sort) {
        this.origin = origin;
        this.headerNames = headerNames;
        this.sort = sort;
    }

    @Transactional(value = "processingTransactionManager", propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void exportStreamToCsv(String search, String key, OutputStream outputStream) {
        log.info("({}) - HikariPool Idle Connections before exportStreamToCsv: {}", this.getClass(),
                this.getProcessingDataSourcePoolMetadata().getIdle());
        Optional<FileDownloadKey> optDownloadKey = findDownloadKey(key);
        if (optDownloadKey.isPresent()) {
            Instant start = Instant.now();
            FileDownloadKey fileDownloadKey = optDownloadKey.get();
            boolean autoCommit = getCurrentSessionAutoCommitPropertyAndSetFalse();
            boolean readOnly = getCurrentSessionReadOnlyPropertyAndSetTrue();
            TenantContext.setTenant(fileDownloadKey.getTenantId());
            SecurityContextHolder.getContext().setAuthentication(fileDownloadKey.getSerializedAuthentication());
            TypedQuery<Tuple> typedQuery = buildTypedQuery(search);
            try (CSVWriterWrapper csvWriter = new CSVWriterWrapper(outputStream);
                 Stream<Tuple> streamData = typedQuery.getResultStream()) {
                csvWriter.writeNext(headerNames);
                streamData.forEach(d -> csvWriter.writeNext(this.toStringArray(d)));
                csvWriter.flush();
            }
            restoreAutoCommitAndReadOnly(autoCommit, readOnly);
            TenantContext.clearTenant();
            SecurityContextHolder.clearContext();
            Instant end = Instant.now();
            log.info("({}) - Download processed in {} seconds", this.getClass(), Duration.between(start, end).toSeconds());
        }
    }

    private Optional<FileDownloadKey> findDownloadKey(String key) {
        try {
            return Optional.of(this.getFileDownloadKeyService().findDownloadKeyTypedQuery(key, origin, this.getProcessingEntityManager()));
        } catch (ObjectNotValidException | ObjectNotFoundException e) {
            log.error(e.getMessage());
        }
        return Optional.empty();
    }

    private boolean getCurrentSessionAutoCommitPropertyAndSetFalse() {
        Session session = this.getProcessingEntityManager().unwrap(Session.class);
        boolean currentAutoCommit = session.doReturningWork(Connection::getAutoCommit);
        session.doWork(connection -> connection.setAutoCommit(false));
        return currentAutoCommit;
    }

    private boolean getCurrentSessionReadOnlyPropertyAndSetTrue() {
        Session session = this.getProcessingEntityManager().unwrap(Session.class);
        boolean currentReadOnly = session.doReturningWork(Connection::isReadOnly);
        session.doWork(connection -> connection.setReadOnly(true));
        return currentReadOnly;
    }

    private TypedQuery<Tuple> buildTypedQuery(String search) {
        CriteriaBuilder builder = this.getProcessingEntityManager().getCriteriaBuilder();
        CriteriaQuery<Tuple> query = builder.createTupleQuery();
        Root<T> root = query.from(modelType);
        query.multiselect(this.buildSelections(root, query, builder));
        Specification<T> spec = this.buildSpecification(search);
        query.where(spec.toPredicate(root, query, builder));
        if (this.getSort() != null && this.getSort().isSorted())
            query.orderBy(toOrders(sort, root, builder));
        TypedQuery<Tuple> typedQuery = this.getProcessingEntityManager().createQuery(query);
        Map<String, Object> queryHints = buildQueryHints();
        queryHints.forEach(typedQuery::setHint);
        return typedQuery;
    }

    /**
     * HINT_FETCH_SIZE ignored if autoCommit = true.
     * If pool autoCommit is true then it should be disable for method execution
     **/
    private Map<String, Object> buildQueryHints() {
        Map<String, Object> queryHints = new HashMap<>();
        queryHints.put(HINT_FETCH_SIZE, "5000");
        queryHints.put(HINT_READONLY, true);
        queryHints.put(HINT_CACHEABLE, true);
        return queryHints;
    }

    @Override
    public Specification<T> buildSpecification(String search) {
        return (root, query, builder) -> {
            Predicate tenantPredicate = builder.and(builder.equal(root.get("tenantId"), TenantContext.getTenant()),
                    builder.equal(root.get("deleted"), false));
            Optional<Specification<T>> searchSpec = baseServiceFinder.buildSpecification(search);
            if (searchSpec.isPresent())
                return builder.and(tenantPredicate, searchSpec.get().toPredicate(root, query, builder));
            return tenantPredicate;
        };
    }

    private void restoreAutoCommitAndReadOnly(boolean autoCommit, boolean readOnly) {
        Session session = this.getProcessingEntityManager().unwrap(Session.class);
        session.doWork(connection -> {
            connection.setAutoCommit(autoCommit);
            connection.setReadOnly(readOnly);
        });
    }

    public EntityManager getProcessingEntityManager() {
        return this.processingEntityManager;
    }

    public FileDownloadKeyService getFileDownloadKeyService() {
        return this.fileDownloadKeyService;
    }

    public HikariDataSourcePoolMetadata getProcessingDataSourcePoolMetadata() {
        return this.processingDataSourcePoolMetadata;
    }

    public Sort getSort() {
        return this.sort;
    }

}
