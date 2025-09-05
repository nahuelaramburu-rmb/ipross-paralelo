package com.capacidad.validationapi.config.security;

import com.capacidad.validationapi.config.filter.JWTAuthenticationFilter;
import com.capacidad.validationapi.misc.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.ForwardedHeaderFilter;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.*;
import static com.capacidad.validationapi.misc.constant.ScopeConstants.*;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.*;


@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String ID_PATH_VARIABLE = "/{\\d+}";
    private static final String ALL_SUBRESOURCES = "/*";
    private final JWTAuthenticationFilter authenticationFilter;

    public WebSecurityConfig(JWTAuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers(HttpMethod.GET, "/*/data/**")
                .antMatchers(StringUtils.join(ENDPOINT_WEBSOCKET_AUDIT, ALL_PARAMS))
                .antMatchers(StringUtils.join(ENDPOINT_WEBSOCKET_NOTIFICATIONS, ALL_PARAMS));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().cors().and().authorizeRequests()
                //Actuator
                .antMatchers(HttpMethod.GET, ENDPOINT_HEALTH).permitAll()
                .antMatchers(StringUtils.join(ENDPOINT_ACTUATOR, ALL_PARAMS)).hasRole(ADMIN)

                //Adjustments
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_ADJUSTMENTS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, CONTRACTS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_ADJUSTMENTS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, CONTRACTS))

                //AuditHistory
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_AUDIT_HISTORY, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, AUDIT_HISTORY))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_AUDIT_HISTORY, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, AUDIT_HISTORY))

                //AuditTrays & Auditors
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_AUDIT_TRAYS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, AUDIT_TRAYS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_AUDIT_TRAYS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, AUDIT_TRAYS))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_AUDIT_TRAYS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, AUDIT_TRAYS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_AUDIT_TRAYS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, AUDIT_TRAYS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_AUDIT_TRAYS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, AUDIT_TRAYS))
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_AUDITORS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, AUDIT_TRAYS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_AUDITORS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, AUDIT_TRAYS))

                //Beneficiaries
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_BENEFICIARIES, ENDPOINT_AUTH)).hasRole(BENEFICIARY)
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_BENEFICIARIES, ENDPOINT_VERIFICATION)).hasAuthority(SecurityUtils.buildScope(CREATE, USERS))
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_BENEFICIARIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, BENEFICIARIES))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_BENEFICIARIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, BENEFICIARIES))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_BENEFICIARIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, BENEFICIARIES))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_BENEFICIARIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, BENEFICIARIES))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_BENEFICIARIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, BENEFICIARIES))

                //BeneficiaryCategories && PractitionerCategories
                .antMatchers(HttpMethod.GET, StringUtils.join(ALL_PARAMS, ENDPOINT_CATEGORIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, CATEGORIES))
                .antMatchers(HttpMethod.POST, StringUtils.join(ALL_PARAMS, ENDPOINT_CATEGORIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, CATEGORIES))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ALL_PARAMS, ENDPOINT_CATEGORIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, CATEGORIES))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ALL_PARAMS, ENDPOINT_CATEGORIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, CATEGORIES))

                //Batches & Batch Items
                .antMatchers(HttpMethod.GET, StringUtils.join(ALL_PARAMS, ENDPOINT_BATCHES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, BATCHES))
                .antMatchers(HttpMethod.POST, StringUtils.join(ALL_PARAMS, ENDPOINT_BATCHES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, BATCHES))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ALL_PARAMS, ENDPOINT_BATCHES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, BATCHES))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ALL_PARAMS, ENDPOINT_BATCHES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, BATCHES))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ALL_PARAMS, ENDPOINT_BATCHES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, BATCHES))
                .antMatchers(HttpMethod.GET, StringUtils.join(ALL_PARAMS, ENDPOINT_BATCH_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, BATCHES))
                .antMatchers(HttpMethod.POST, StringUtils.join(ALL_PARAMS, ENDPOINT_BATCH_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, BATCHES))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ALL_PARAMS, ENDPOINT_BATCH_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, BATCHES))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ALL_PARAMS, ENDPOINT_BATCH_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, BATCHES))

                //Beneficiary Budgets
                .antMatchers(HttpMethod.GET, StringUtils.join(ALL_PARAMS, ENDPOINT_BENEFICIARY_BUDGETS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, BENEFICIARY_BUDGETS))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ALL_PARAMS, ENDPOINT_BENEFICIARY_BUDGETS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, BENEFICIARY_BUDGETS))

                //Beneficiary Insurance Plans
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_BENEFICIARY_INSURANCE_PLANS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, BENEFICIARIES))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_BENEFICIARY_INSURANCE_PLANS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, BENEFICIARIES))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_BENEFICIARY_INSURANCE_PLANS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, BENEFICIARIES))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_BENEFICIARY_INSURANCE_PLANS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, BENEFICIARIES))

                //Practitioner Budgets
                .antMatchers(HttpMethod.GET, StringUtils.join(ALL_PARAMS, ENDPOINT_PRACTITIONER_BUDGETS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, PRACTITIONER_BUDGETS))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ALL_PARAMS, ENDPOINT_PRACTITIONER_BUDGETS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, PRACTITIONER_BUDGETS))

                //Calendar
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_CALENDAR, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, CALENDAR))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_CALENDAR, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, CALENDAR))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_CALENDAR, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, CALENDAR))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_CALENDAR, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, CALENDAR))


                //Contracts && ContractItems
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_CONTRACTS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, CONTRACTS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_CONTRACTS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, CONTRACTS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_CONTRACTS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, CONTRACTS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_CONTRACTS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, CONTRACTS))
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_CONTRACT_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, CONTRACTS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_CONTRACT_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, CONTRACTS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_CONTRACT_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, CONTRACTS))

                //Company
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_COMPANIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, COMPANIES))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_COMPANIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, COMPANIES))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_COMPANIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, COMPANIES))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_COMPANIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, COMPANIES))

                //Dashboards
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_DASHBOARDS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, REPORTS))

                //Expirations
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_EXPIRATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, EXPIRATIONS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_EXPIRATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, EXPIRATIONS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_EXPIRATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, EXPIRATIONS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_EXPIRATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, EXPIRATIONS))

                //Insurance Plans
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_INSURANCE_PLANS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, INSURANCE_PLANS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_INSURANCE_PLANS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, INSURANCE_PLANS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_INSURANCE_PLANS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, INSURANCE_PLANS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_INSURANCE_PLANS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, INSURANCE_PLANS))

                //Medical Authorizations
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_AUTHORIZATION_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, AUTHORIZATION_ITEMS))
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_AUTHORIZATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, AUTHORIZATIONS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_AUTHORIZATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, AUTHORIZATIONS))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_AUTHORIZATIONS, ID_PATH_VARIABLE, "/rating")).hasRole(BENEFICIARY)
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_AUTHORIZATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, AUTHORIZATIONS))

                //Medical Centers
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_MEDICAL_CENTERS, ENDPOINT_AUTH)).hasRole(MEDICAL_CENTER)
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_MEDICAL_CENTERS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, MEDICAL_CENTERS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_MEDICAL_CENTERS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, MEDICAL_CENTERS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_MEDICAL_CENTERS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, MEDICAL_CENTERS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_MEDICAL_CENTERS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, MEDICAL_CENTERS))

                //Medical Coverages
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_MEDICAL_COVERAGES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, MEDICAL_COVERAGES))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_MEDICAL_COVERAGES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, MEDICAL_COVERAGES))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_MEDICAL_COVERAGES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, MEDICAL_COVERAGES))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_MEDICAL_COVERAGES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, MEDICAL_COVERAGES))
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_MEDICAL_COVERAGE_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, MEDICAL_COVERAGES))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_MEDICAL_COVERAGE_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, MEDICAL_COVERAGES))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_MEDICAL_COVERAGE_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, MEDICAL_COVERAGES))

                //Medical Registrations
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_MEDICAL_REGISTRATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, PRACTITIONERS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_MEDICAL_REGISTRATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, PRACTITIONERS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_MEDICAL_REGISTRATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, PRACTITIONERS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_MEDICAL_REGISTRATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, PRACTITIONERS))

                //Medicines
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_MEDICINES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, MEDICINES))

                //Nomenclators
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_NOMENCLATORS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, NOMENCLATORS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_NOMENCLATORS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, NOMENCLATORS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_NOMENCLATORS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, NOMENCLATORS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_NOMENCLATORS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, NOMENCLATORS))

                //NomenclatorGroups
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_NOMENCLATOR_GROUPS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, NOMENCLATORS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_NOMENCLATOR_GROUPS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, NOMENCLATORS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_NOMENCLATOR_GROUPS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, NOMENCLATORS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_NOMENCLATOR_GROUPS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, NOMENCLATORS))

                //Notifications
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_NOTIFICATION_PUBLICATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, NOTIFICATION_PUBLICATIONS))

                //Organizations
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_ORGANIZATIONS, ENDPOINT_AUTH)).hasRole(ORGANIZATION)
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_ORGANIZATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, ORGANIZATIONS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_ORGANIZATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, ORGANIZATIONS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_ORGANIZATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, ORGANIZATIONS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_ORGANIZATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, ORGANIZATIONS))

                //Practitioners
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_PRACTITIONERS, ENDPOINT_KEY)).hasRole(PRACTITIONER)
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_PRACTITIONERS, ENDPOINT_AUTH)).hasRole(PRACTITIONER)
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_PRACTITIONERS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, PRACTITIONERS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_PRACTITIONERS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, PRACTITIONERS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_PRACTITIONERS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, PRACTITIONERS))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_PRACTITIONERS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, PRACTITIONERS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_PRACTITIONERS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, PRACTITIONERS))

                //Prescriptions
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_PRESCRIPTIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, PRESCRIPTIONS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_PRESCRIPTIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, PRESCRIPTIONS))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_PRESCRIPTIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, PRESCRIPTIONS))

                //PrescriptionRestrictions
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_PRESCRIPTION_RESTRICTIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, PRESCRIPTION_RESTRICTIONS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_PRESCRIPTION_RESTRICTIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, PRESCRIPTION_RESTRICTIONS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_PRESCRIPTION_RESTRICTIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, PRESCRIPTION_RESTRICTIONS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_PRESCRIPTION_RESTRICTIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, PRESCRIPTION_RESTRICTIONS))

                //Procedures
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_PROCEDURES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, PROCEDURES))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_PROCEDURES, ALL_SUBRESOURCES, ID_PATH_VARIABLE, "/tags")).hasAuthority(SecurityUtils.buildScope(UPDATE, PROCEDURES))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_PROCEDURES, ALL_SUBRESOURCES, ID_PATH_VARIABLE)).hasAuthority(SecurityUtils.buildScope(UPDATE, PROCEDURES))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_PROCEDURES, ALL_SUBRESOURCES, ID_PATH_VARIABLE, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, PROCEDURES))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_PROCEDURES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, PROCEDURES))

                //Properties
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_PROPERTIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, PROPERTIES))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_PROPERTIES, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, PROPERTIES))

                //PreMedicalAuthorizations
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_PRE_AUTHORIZATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, PRE_AUTHORIZATIONS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_PRE_AUTHORIZATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, PRE_AUTHORIZATIONS))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_PRE_AUTHORIZATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, PRE_AUTHORIZATIONS))

                //Regions
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_REGIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, REGIONS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_REGIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, REGIONS))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_REGIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, REGIONS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_REGIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, REGIONS))

                //Rules
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_RULE_CONFIGURATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, RULES))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_RULE_CONFIGURATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, RULES))
                .antMatchers(HttpMethod.PATCH, StringUtils.join(ENDPOINT_RULE_CONFIGURATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, RULES))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_RULE_CONFIGURATIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, RULES))

                //Settlements
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_SETTLEMENTS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, SETTLEMENTS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_SETTLEMENTS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, SETTLEMENTS))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_SETTLEMENTS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, SETTLEMENTS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_SETTLEMENTS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, SETTLEMENTS))

                //Storage
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_STORAGE, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, FILES))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_STORAGE, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, FILES))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_STORAGE, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, FILES))

                //TradeUnions
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_TRADE_UNIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, TRADE_UNIONS))
                .antMatchers(HttpMethod.POST, StringUtils.join(ENDPOINT_TRADE_UNIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(CREATE, TRADE_UNIONS))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_TRADE_UNIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, TRADE_UNIONS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_TRADE_UNIONS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, TRADE_UNIONS))
                .antMatchers(HttpMethod.GET, StringUtils.join(ENDPOINT_TRADE_UNION_COVERAGE_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(READ, TRADE_UNIONS))
                .antMatchers(HttpMethod.PUT, StringUtils.join(ENDPOINT_TRADE_UNION_COVERAGE_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(UPDATE, TRADE_UNIONS))
                .antMatchers(HttpMethod.DELETE, StringUtils.join(ENDPOINT_TRADE_UNION_COVERAGE_ITEMS, ALL_PARAMS)).hasAuthority(SecurityUtils.buildScope(DELETE, TRADE_UNIONS))

                //All
                .anyRequest().authenticated()
                .and()
                //Filters
                .addFilterBefore(authenticationFilter, BasicAuthenticationFilter.class)
                //Session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}
