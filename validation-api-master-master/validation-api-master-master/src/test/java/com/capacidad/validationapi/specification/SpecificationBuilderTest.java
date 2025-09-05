package com.capacidad.validationapi.specification;

import com.capacidad.validationapi.module.beneficiary.model.Beneficiary;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SpecificationBuilderTest {

    @InjectMocks
    private SpecificationBuilder<Beneficiary, Long> specificationBuilder;

    @Test
    public void testParseAndBuildReturnsEmptyWhenSearchIsEmpty() {
        Optional<Specification<Beneficiary>> spec = specificationBuilder.parseAndBuild("");

        assertThat(spec.isEmpty()).isTrue();
    }

    @Test
    public void testParseAndBuildReturnsEmptyWhenSearchIsInvalid() {
        Optional<Specification<Beneficiary>> spec = specificationBuilder.parseAndBuild("@invalidSearch@");

        assertThat(spec.isEmpty()).isTrue();
    }

    @Test
    public void testParseAndBuildReturnsEmptyWhenSearchKeyIsId() {
        Optional<Specification<Beneficiary>> spec = specificationBuilder.parseAndBuild("id:1");

        assertThat(spec.isEmpty()).isFalse();
    }

    @Test
    public void testParseAndBuildReturnsSpecWhenSearchIsSimple() {
        Optional<Specification<Beneficiary>> spec = specificationBuilder.parseAndBuild("name:test");

        ModelSpecification<Beneficiary, Long> modelSpecification = (ModelSpecification<Beneficiary, Long>) spec.orElse(null);
        SearchCriteria criteria = modelSpecification.getCriteria();

        assertThat(criteria.getKey()).isEqualTo("name");
        assertThat(criteria.getOperation()).isEqualTo(SearchOperation.EQUALITY);
        assertThat(criteria.getValue()).isEqualTo("test");
    }

    @Test
    public void testParseAndBuildReturnsSpecWhenSearchIsMultiple() {
        Optional<Specification<Beneficiary>> spec = specificationBuilder.parseAndBuild("name:test,lastName:test1");

        Specification<Beneficiary> specification = spec.orElse(null);

        assertThat(specification).isNotNull();
    }

    @Test
    public void testParseAndBuildReturnsSpecWhenSearchIsCompoundAndStartsWith() {
        Optional<Specification<Beneficiary>> spec = specificationBuilder.parseAndBuild("status:{name=test*}");

        ModelSpecification<Beneficiary, Long> modelSpecification = (ModelSpecification<Beneficiary, Long>) spec.orElse(null);
        SearchCriteria criteria = modelSpecification.getCriteria();

        assertThat(criteria.getKey()).isEqualTo("status");
        assertThat(criteria.getOperation()).isEqualTo(SearchOperation.STARTS_WITH);
        assertThat(criteria.getValue()).isInstanceOf(SearchCriteria.class);
        assertThat(((SearchCriteria) criteria.getValue()).getKey()).isEqualTo("name");
        assertThat(((SearchCriteria) criteria.getValue()).getValue()).isEqualTo("test");
    }

    @Test
    public void testParseAndBuildReturnsSpecWhenSearchIsCompoundAndEndsWith() {
        Optional<Specification<Beneficiary>> spec = specificationBuilder.parseAndBuild("status:{name=*test}");

        ModelSpecification<Beneficiary, Long> modelSpecification = (ModelSpecification<Beneficiary, Long>) spec.orElse(null);
        SearchCriteria criteria = modelSpecification.getCriteria();

        assertThat(criteria.getKey()).isEqualTo("status");
        assertThat(criteria.getOperation()).isEqualTo(SearchOperation.ENDS_WITH);
        assertThat(criteria.getValue()).isInstanceOf(SearchCriteria.class);
        assertThat(((SearchCriteria) criteria.getValue()).getKey()).isEqualTo("name");
        assertThat(((SearchCriteria) criteria.getValue()).getValue()).isEqualTo("test");
    }

    @Test
    public void testParseAndBuildReturnsSpecWhenSearchIsCompoundAndContains() {
        Optional<Specification<Beneficiary>> spec = specificationBuilder.parseAndBuild("status:{name=*test*}");

        ModelSpecification<Beneficiary, Long> modelSpecification = (ModelSpecification<Beneficiary, Long>) spec.orElse(null);
        SearchCriteria criteria = modelSpecification.getCriteria();

        assertThat(criteria.getKey()).isEqualTo("status");
        assertThat(criteria.getOperation()).isEqualTo(SearchOperation.CONTAINS);
        assertThat(criteria.getValue()).isInstanceOf(SearchCriteria.class);
        assertThat(((SearchCriteria) criteria.getValue()).getKey()).isEqualTo("name");
        assertThat(((SearchCriteria) criteria.getValue()).getValue()).isEqualTo("test");
    }

    @Test
    public void testParseAndBuildReturnsSpecWhenSearchIsCompound() {
        Optional<Specification<Beneficiary>> spec = specificationBuilder.parseAndBuild("status~{name=test}");

        ModelSpecification<Beneficiary, Long> modelSpecification = (ModelSpecification<Beneficiary, Long>) spec.orElse(null);
        SearchCriteria criteria = modelSpecification.getCriteria();

        assertThat(criteria.getKey()).isEqualTo("status");
        assertThat(criteria.getOperation()).isEqualTo(SearchOperation.LIKE);
        assertThat(criteria.getValue()).isInstanceOf(SearchCriteria.class);
        assertThat(((SearchCriteria) criteria.getValue()).getKey()).isEqualTo("name");
        assertThat(((SearchCriteria) criteria.getValue()).getValue()).isEqualTo("test");
    }

    @Test
    public void testParseAndBuildReturnsSpecWhenSearchIsReferenced() {
        Optional<Specification<Beneficiary>> spec = specificationBuilder.parseAndBuild("certificates.certificateType~{name=cert1}");

        ModelSpecification<Beneficiary, Long> modelSpecification = (ModelSpecification<Beneficiary, Long>) spec.orElse(null);
        SearchCriteria criteria = modelSpecification.getCriteria();

        assertThat(criteria.getKey()).isEqualTo("certificates.certificateType");
        assertThat(criteria.getOperation()).isEqualTo(SearchOperation.LIKE);
        assertThat(criteria.getValue()).isInstanceOf(SearchCriteria.class);
        assertThat(((SearchCriteria) criteria.getValue()).getKey()).isEqualTo("name");
        assertThat(((SearchCriteria) criteria.getValue()).getValue()).isEqualTo("cert1");
    }

}
