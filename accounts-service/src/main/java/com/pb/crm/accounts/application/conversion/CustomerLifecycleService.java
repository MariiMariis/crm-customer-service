package com.pb.crm.accounts.application.conversion;

import com.pb.crm.accounts.application.events.AccountsEventRecorder;
import com.pb.crm.accounts.domain.company.Company;
import com.pb.crm.accounts.domain.company.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomerLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(CustomerLifecycleService.class);

    private final CompanyRepository companyRepository;
    private final AccountsEventRecorder recorder;

    public CustomerLifecycleService(CompanyRepository companyRepository, AccountsEventRecorder recorder) {
        this.companyRepository = companyRepository;
        this.recorder = recorder;
    }

    @Transactional
    public boolean onOpportunityWon(Long companyId, Long opportunityId) {
        Optional<Company> found = companyRepository.findById(companyId);
        if (found.isEmpty()) {
            log.warn("Oportunidade #{} ganha para a empresa #{}, que nao existe no servico de contas", opportunityId, companyId);
            return false;
        }
        Company company = found.get();
        if (!company.promoteToCustomer()) {
            log.info("Empresa #{} mantida como {} apos a oportunidade #{} ganha", companyId,
                    company.getProfile().type(), opportunityId);
            return false;
        }
        recorder.save(company);
        log.info("Empresa #{} promovida a CUSTOMER pela oportunidade #{} ganha", companyId, opportunityId);
        return true;
    }
}
