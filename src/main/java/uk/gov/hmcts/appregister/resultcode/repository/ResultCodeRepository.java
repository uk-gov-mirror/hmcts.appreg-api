package uk.gov.hmcts.appregister.resultcode.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uk.gov.hmcts.appregister.resultcode.model.ResultCode;

public interface ResultCodeRepository
    extends JpaRepository<ResultCode, Long>,
    JpaSpecificationExecutor<ResultCode> {
    Optional<ResultCode> findByResultCode(String code);
}
