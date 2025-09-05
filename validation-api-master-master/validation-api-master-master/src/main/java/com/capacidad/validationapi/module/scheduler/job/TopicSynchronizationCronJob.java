package com.capacidad.validationapi.module.scheduler.job;

import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import com.capacidad.validationapi.module.location.model.Address;
import com.capacidad.validationapi.module.location.model.City;
import com.capacidad.validationapi.module.notification.service.NotificationPublisher;
import com.capacidad.validationapi.module.person.model.Gender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.SLASH;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.*;
import static org.hibernate.jpa.QueryHints.*;

@Log4j2
@DisallowConcurrentExecution
@Component
public class TopicSynchronizationCronJob implements Job {

    @PersistenceContext(unitName = "processing")
    private EntityManager processingEntityManager;
    private final NotificationPublisher notificationPublisher;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ObjectNode mainNode;
    private static final String RESOURCE_ID_COLUMN_NAME = "resourceId";
    private static final String DELETED_COLUMN_NAME = "deleted";
    private static final String RESOURCE_ID_DATA_TYPE = "resource_id";
    private static final String TENANT_ID_DATA_TYPE = "tenantId";
    private static final String AGE_FROM_FUNCTION = "age_from_birthdate";
    private static final String BIRTH_DATE_COLUMN = "birthDate";

    @Autowired
    public TopicSynchronizationCronJob(NotificationPublisher notificationPublisher) {
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        this.mainNode = OBJECT_MAPPER.createObjectNode();
        this.notificationPublisher = notificationPublisher;
    }

    @Transactional(value = "processingTransactionManager", propagation = Propagation.NOT_SUPPORTED)
    @Override
    public void execute(JobExecutionContext context) {
        Instant start = Instant.now();
        try {
            File file = new File(StringUtils.join(System.getProperty("java.io.tmpdir"), SLASH, "topic_output.json"));
            boolean autoCommit = getCurrentSessionAutoCommitPropertyAndSetFalse();
            boolean readOnly = getCurrentSessionReadOnlyPropertyAndSetTrue();
            collectDataAndSaveInFile(file);
            notificationPublisher.publishToTopic(file);
            Files.delete(file.toPath());
            restoreAutoCommitAndReadOnly(autoCommit, readOnly);
        } catch (IOException e) {
            log.error("{} - executeQueriesAndStreamToFile: {} ", this.getClass(), e.getMessage());
        }
        Instant end = Instant.now();
        log.info("Cron ({}) Execution duration: {} millis", this.getClass(), Duration.between(start, end).toMillis());
    }

    private boolean getCurrentSessionAutoCommitPropertyAndSetFalse() {
        Session session = processingEntityManager.unwrap(Session.class);
        boolean currentAutoCommit = session.doReturningWork(Connection::getAutoCommit);
        session.doWork(connection -> connection.setAutoCommit(false));
        return currentAutoCommit;
    }

    private boolean getCurrentSessionReadOnlyPropertyAndSetTrue() {
        Session session = processingEntityManager.unwrap(Session.class);
        boolean currentReadOnly = session.doReturningWork(Connection::isReadOnly);
        session.doWork(connection -> connection.setReadOnly(true));
        return currentReadOnly;
    }

    private void collectDataAndSaveInFile(File file) throws IOException {
        saveBeneficiaryGenderMale(file);
        saveBeneficiaryGenderFemale(file);
        saveBeneficiaryAge0to20(file);
        saveBeneficiaryAge20to60(file);
        saveBeneficiaryAge60over(file);
        saveBeneficiaryCityViedma(file);
        processRoleTopics(file);
    }

    private void saveBeneficiaryGenderMale(File file) throws IOException {
        CriteriaBuilder builder = processingEntityManager.getCriteriaBuilder();
        CriteriaQuery<Object> cq = builder.createQuery();
        Root<Beneficiary> root = cq.from(Beneficiary.class);
        cq.multiselect(root.get(RESOURCE_ID_COLUMN_NAME), root.get(TENANT_ID_DATA_TYPE));
        cq.where(builder.and(builder.equal(root.get("gender"), Gender.MASCULINO)),
                builder.equal(root.get(DELETED_COLUMN_NAME), false));
        cq.groupBy(root.get(RESOURCE_ID_COLUMN_NAME), root.get(TENANT_ID_DATA_TYPE));
        TypedQuery<Object> query = buildTypeQueryWithHints(cq);
        Stream<Object> result = query.getResultStream();
        TopicData topicData = new TopicData("topicMale", null, RESOURCE_ID_DATA_TYPE, null, "Afiliados Masculinos", "");
        writeResourceIdDataTypeJSON(result, file, topicData);
    }

    private void writeResourceIdDataTypeJSON(Stream<Object> data, File file, TopicData topicData) throws IOException {
        Map<String, ArrayNode> dataMap = new HashMap<>();
        data.forEach(dataItem -> {
            Object[] arrayData = (Object[]) dataItem;
            ArrayNode tenantDataNode = createOrGetTenantNode(arrayData[1].toString(), dataMap);
            tenantDataNode.add(arrayData[0].toString());
        });
        ArrayNode topicArrayNode = OBJECT_MAPPER.createArrayNode();
        for (Map.Entry<String, ArrayNode> o : dataMap.entrySet()) {
            topicData.setTenantId(o.getKey());
            topicData.setData(o.getValue());
            topicArrayNode.add(topicData.buildObjectNode());
        }
        mainNode.set(topicData.getTopicName(), topicArrayNode);
        OBJECT_MAPPER.writeValue(file, mainNode);
    }

    private ArrayNode createOrGetTenantNode(String key, Map<String, ArrayNode> topicMap) {
        return topicMap.computeIfAbsent(key, k -> OBJECT_MAPPER.createArrayNode());
    }

    /**
     * HINT_FETCH_SIZE ignored if autoCommit = true.
     * If pool autoCommit is true then it should be disable for method execution
     **/
    private TypedQuery<Object> buildTypeQueryWithHints(CriteriaQuery<Object> criteriaQuery) {
        Map<String, Object> queryHints = new HashMap<>();
        queryHints.put(HINT_FETCH_SIZE, "5000");
        queryHints.put(HINT_READONLY, "true");
        queryHints.put(HINT_CACHEABLE, "true");
        TypedQuery<Object> query = processingEntityManager.createQuery(criteriaQuery);
        queryHints.forEach(query::setHint);
        return query;
    }

    private void saveBeneficiaryGenderFemale(File file) throws IOException {
        CriteriaBuilder builder = processingEntityManager.getCriteriaBuilder();
        CriteriaQuery<Object> cq = builder.createQuery();
        Root<Beneficiary> root = cq.from(Beneficiary.class);
        cq.multiselect(root.get(RESOURCE_ID_COLUMN_NAME), root.get(TENANT_ID_DATA_TYPE));
        cq.where(builder.and(builder.equal(root.get("gender"), Gender.FEMENINO)),
                builder.equal(root.get(DELETED_COLUMN_NAME), false));
        cq.groupBy(root.get(RESOURCE_ID_COLUMN_NAME), root.get(TENANT_ID_DATA_TYPE));
        TypedQuery<Object> query = buildTypeQueryWithHints(cq);
        Stream<Object> result = query.getResultStream();
        TopicData topicData = new TopicData("topicFemale", null, RESOURCE_ID_DATA_TYPE, null, "Afiliados Femeninos", "");
        writeResourceIdDataTypeJSON(result, file, topicData);
    }

    private void saveBeneficiaryAge0to20(File file) throws IOException {
        CriteriaBuilder builder = processingEntityManager.getCriteriaBuilder();
        CriteriaQuery<Object> cq = builder.createQuery();
        Root<Beneficiary> root = cq.from(Beneficiary.class);
        cq.multiselect(root.get(RESOURCE_ID_COLUMN_NAME), root.get(TENANT_ID_DATA_TYPE));
        cq.where(builder.and(
                builder.greaterThanOrEqualTo(builder.function(AGE_FROM_FUNCTION, Integer.class, root.get(BIRTH_DATE_COLUMN)), 0),
                builder.lessThan(builder.function(AGE_FROM_FUNCTION, Integer.class, root.get(BIRTH_DATE_COLUMN)), 20)),
                builder.equal(root.get(DELETED_COLUMN_NAME), false));
        cq.groupBy(root.get(RESOURCE_ID_COLUMN_NAME), root.get(TENANT_ID_DATA_TYPE));
        TypedQuery<Object> query = buildTypeQueryWithHints(cq);
        Stream<Object> result = query.getResultStream();
        TopicData topicData = new TopicData("topicBetween0to20", null, RESOURCE_ID_DATA_TYPE, null, "Afiliados entre 0 y 20 años", "");
        writeResourceIdDataTypeJSON(result, file, topicData);
    }

    private void saveBeneficiaryAge20to60(File file) throws IOException {
        CriteriaBuilder builder = processingEntityManager.getCriteriaBuilder();
        CriteriaQuery<Object> cq = builder.createQuery();
        Root<Beneficiary> root = cq.from(Beneficiary.class);
        cq.multiselect(root.get(RESOURCE_ID_COLUMN_NAME), root.get(TENANT_ID_DATA_TYPE));
        cq.where(builder.and(
                builder.greaterThanOrEqualTo(builder.function(AGE_FROM_FUNCTION, Integer.class, root.get(BIRTH_DATE_COLUMN)), 20),
                builder.lessThan(builder.function(AGE_FROM_FUNCTION, Integer.class, root.get(BIRTH_DATE_COLUMN)), 60)),
                builder.equal(root.get(DELETED_COLUMN_NAME), false));
        cq.groupBy(root.get(RESOURCE_ID_COLUMN_NAME), root.get(TENANT_ID_DATA_TYPE));
        TypedQuery<Object> query = buildTypeQueryWithHints(cq);
        Stream<Object> result = query.getResultStream();
        TopicData topicData = new TopicData("topicBetween20to60", null, RESOURCE_ID_DATA_TYPE, null, "Afiliados entre 20 y 60 años", "");
        writeResourceIdDataTypeJSON(result, file, topicData);
    }

    private void saveBeneficiaryAge60over(File file) throws IOException {
        CriteriaBuilder builder = processingEntityManager.getCriteriaBuilder();
        CriteriaQuery<Object> cq = builder.createQuery();
        Root<Beneficiary> root = cq.from(Beneficiary.class);
        cq.multiselect(root.get(RESOURCE_ID_COLUMN_NAME), root.get(TENANT_ID_DATA_TYPE));
        cq.where(builder.and(builder.greaterThanOrEqualTo(builder.function(AGE_FROM_FUNCTION, Integer.class, root.get(BIRTH_DATE_COLUMN)), 60)),
                builder.equal(root.get(DELETED_COLUMN_NAME), false));
        cq.groupBy(root.get(RESOURCE_ID_COLUMN_NAME), root.get(TENANT_ID_DATA_TYPE));
        TypedQuery<Object> query = buildTypeQueryWithHints(cq);
        Stream<Object> result = query.getResultStream();
        TopicData topicData = new TopicData("topicOver60", null, RESOURCE_ID_DATA_TYPE, null, "Afiliados mayores a 60 años", "");
        writeResourceIdDataTypeJSON(result, file, topicData);
    }

    private void saveBeneficiaryCityViedma(File file) throws IOException {
        CriteriaBuilder builder = processingEntityManager.getCriteriaBuilder();
        CriteriaQuery<Object> cq = builder.createQuery();
        Root<Beneficiary> root = cq.from(Beneficiary.class);
        cq.multiselect(root.get(RESOURCE_ID_COLUMN_NAME), root.get(TENANT_ID_DATA_TYPE));
        Join<Beneficiary, Address> addressJoin = root.join("address");
        Join<Address, City> cityJoin = addressJoin.join("city");
        cq.where(builder.and(builder.equal(cityJoin.get("name"), "Viedma")),
                builder.equal(root.get(DELETED_COLUMN_NAME), false));
        cq.groupBy(root.get(RESOURCE_ID_COLUMN_NAME), root.get(TENANT_ID_DATA_TYPE));
        TypedQuery<Object> query = buildTypeQueryWithHints(cq);
        Stream<Object> result = query.getResultStream();
        TopicData topicData = new TopicData("topicCityViedma", null, RESOURCE_ID_DATA_TYPE, null, "Afiliados de Viedma", "");
        writeResourceIdDataTypeJSON(result, file, topicData);
    }

    private void processRoleTopics(File file) throws IOException {
        List<Object> allTenants = findAllTenants();
        TopicData allBeneficiaries = new TopicData("topicAllBeneficiaries", null, "role", BENEFICIARY, "Todos los afiliados", "");
        processRoleTopics(allTenants, file, allBeneficiaries);
        TopicData allPractitioners = new TopicData("topicAllPractitioners", null, "role", PRACTITIONER, "Todos los prestadores", "");
        processRoleTopics(allTenants, file, allPractitioners);
        TopicData allMedicalCenters = new TopicData("topicAllMedicalCenters", null, "role", MEDICAL_CENTER, "Todos los centros medicos", "");
        processRoleTopics(allTenants, file, allMedicalCenters);
    }

    private void processRoleTopics(List<Object> tenantData, File file, TopicData topicData) throws IOException {
        ArrayNode roleTopicArray = OBJECT_MAPPER.createArrayNode();
        tenantData.forEach(td -> {
            String tenantId = ((Object[]) td)[1].toString();
            topicData.setTenantId(tenantId);
            roleTopicArray.add(topicData.buildObjectNode());
        });
        mainNode.set(topicData.getTopicName(), roleTopicArray);
        OBJECT_MAPPER.writeValue(file, mainNode);
    }

    private List<Object> findAllTenants() {
        CriteriaBuilder builder = processingEntityManager.getCriteriaBuilder();
        CriteriaQuery<Object> cq = builder.createQuery();
        Root<Beneficiary> root = cq.from(Beneficiary.class);
        cq.multiselect(builder.count(root.get(TENANT_ID_DATA_TYPE)), root.get(TENANT_ID_DATA_TYPE));
        cq.groupBy(root.get(TENANT_ID_DATA_TYPE));
        TypedQuery<Object> query = buildTypeQueryWithHints(cq);
        return query.getResultList();
    }

    private void restoreAutoCommitAndReadOnly(boolean autoCommit, boolean readOnly) {
        Session session = processingEntityManager.unwrap(Session.class);
        session.doWork(connection -> {
            connection.setAutoCommit(autoCommit);
            connection.setReadOnly(readOnly);
        });
    }

    @Getter
    @Setter
    static class TopicData {

        private String topicName;
        private String tenantId;
        private String dataType;
        private Object data;
        private String displayName;
        private String description;

        public TopicData(String topicName, String tenantId, String dataType, Object data, String displayName, String description) {
            this.topicName = topicName;
            this.tenantId = tenantId;
            this.dataType = dataType;
            this.data = data;
            this.displayName = displayName;
            this.description = description;
        }

        public ObjectNode buildObjectNode() {
            ObjectNode dataNode = OBJECT_MAPPER.createObjectNode();
            dataNode.put("tenant", this.getTenantId());
            dataNode.put("dataType", this.getDataType());
            dataNode.put("data", "");
            if (StringUtils.equals(this.getDataType(), RESOURCE_ID_DATA_TYPE))
                dataNode.set("data", (ArrayNode) this.getData());
            if (StringUtils.equals(this.getDataType(), "role"))
                dataNode.put("data", (String) this.getData());
            dataNode.put("displayed_name", this.getDisplayName());
            dataNode.put("description", this.getDescription());
            return dataNode;
        }

    }

}
