package com.pb.crm.sales.infrastructure.persistence;

import com.pb.crm.commons.audit.ArchivableEntity;
import com.pb.crm.sales.domain.lead.LeadSource;
import com.pb.crm.sales.domain.lead.LeadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Audited
@Table(name = "leads")
public class LeadJpaEntity extends ArchivableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "leads_seq")
    @SequenceGenerator(name = "leads_seq", sequenceName = "leads_seq", allocationSize = 50)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(length = 160)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "company_name", nullable = false, length = 160)
    private String companyName;

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeadSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeadStatus status;

    @Column(nullable = false)
    private int score;

    @Column(name = "estimated_value", precision = 15, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(length = 2000)
    private String notes;

    @Column(name = "disqualify_reason", length = 500)
    private String disqualifyReason;

    @Column(name = "conversion_cnpj", length = 18)
    private String conversionCnpj;

    @Column(name = "conversion_industry", length = 40)
    private String conversionIndustry;

    @Column(name = "conversion_company_size", length = 20)
    private String conversionCompanySize;

    @Column(name = "conversion_city", length = 100)
    private String conversionCity;

    @Column(name = "conversion_state", length = 2)
    private String conversionState;

    @Column(name = "conversion_create_opportunity")
    private Boolean conversionCreateOpportunity;

    @Column(name = "conversion_opportunity_title", length = 160)
    private String conversionOpportunityTitle;

    @Column(name = "conversion_expected_close_date")
    private LocalDate conversionExpectedCloseDate;

    @Column(name = "conversion_requested_at")
    private Instant conversionRequestedAt;

    @Column(name = "conversion_failure_reason", length = 500)
    private String conversionFailureReason;

    @Column(name = "converted_company_id")
    private Long convertedCompanyId;

    @Column(name = "converted_contact_id")
    private Long convertedContactId;

    @Column(name = "converted_opportunity_id")
    private Long convertedOpportunityId;

    @Column(name = "converted_at")
    private Instant convertedAt;

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public LeadSource getSource() {
        return source;
    }

    public void setSource(LeadSource source) {
        this.source = source;
    }

    public LeadStatus getStatus() {
        return status;
    }

    public void setStatus(LeadStatus status) {
        this.status = status;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public BigDecimal getEstimatedValue() {
        return estimatedValue;
    }

    public void setEstimatedValue(BigDecimal estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDisqualifyReason() {
        return disqualifyReason;
    }

    public void setDisqualifyReason(String disqualifyReason) {
        this.disqualifyReason = disqualifyReason;
    }

    public String getConversionCnpj() {
        return conversionCnpj;
    }

    public void setConversionCnpj(String conversionCnpj) {
        this.conversionCnpj = conversionCnpj;
    }

    public String getConversionIndustry() {
        return conversionIndustry;
    }

    public void setConversionIndustry(String conversionIndustry) {
        this.conversionIndustry = conversionIndustry;
    }

    public String getConversionCompanySize() {
        return conversionCompanySize;
    }

    public void setConversionCompanySize(String conversionCompanySize) {
        this.conversionCompanySize = conversionCompanySize;
    }

    public String getConversionCity() {
        return conversionCity;
    }

    public void setConversionCity(String conversionCity) {
        this.conversionCity = conversionCity;
    }

    public String getConversionState() {
        return conversionState;
    }

    public void setConversionState(String conversionState) {
        this.conversionState = conversionState;
    }

    public Boolean getConversionCreateOpportunity() {
        return conversionCreateOpportunity;
    }

    public void setConversionCreateOpportunity(Boolean conversionCreateOpportunity) {
        this.conversionCreateOpportunity = conversionCreateOpportunity;
    }

    public String getConversionOpportunityTitle() {
        return conversionOpportunityTitle;
    }

    public void setConversionOpportunityTitle(String conversionOpportunityTitle) {
        this.conversionOpportunityTitle = conversionOpportunityTitle;
    }

    public LocalDate getConversionExpectedCloseDate() {
        return conversionExpectedCloseDate;
    }

    public void setConversionExpectedCloseDate(LocalDate conversionExpectedCloseDate) {
        this.conversionExpectedCloseDate = conversionExpectedCloseDate;
    }

    public Instant getConversionRequestedAt() {
        return conversionRequestedAt;
    }

    public void setConversionRequestedAt(Instant conversionRequestedAt) {
        this.conversionRequestedAt = conversionRequestedAt;
    }

    public String getConversionFailureReason() {
        return conversionFailureReason;
    }

    public void setConversionFailureReason(String conversionFailureReason) {
        this.conversionFailureReason = conversionFailureReason;
    }

    public Long getConvertedCompanyId() {
        return convertedCompanyId;
    }

    public void setConvertedCompanyId(Long convertedCompanyId) {
        this.convertedCompanyId = convertedCompanyId;
    }

    public Long getConvertedContactId() {
        return convertedContactId;
    }

    public void setConvertedContactId(Long convertedContactId) {
        this.convertedContactId = convertedContactId;
    }

    public Long getConvertedOpportunityId() {
        return convertedOpportunityId;
    }

    public void setConvertedOpportunityId(Long convertedOpportunityId) {
        this.convertedOpportunityId = convertedOpportunityId;
    }

    public Instant getConvertedAt() {
        return convertedAt;
    }

    public void setConvertedAt(Instant convertedAt) {
        this.convertedAt = convertedAt;
    }
}
