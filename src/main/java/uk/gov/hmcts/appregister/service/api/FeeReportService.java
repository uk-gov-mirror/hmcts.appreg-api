package uk.gov.hmcts.appregister.service.api;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;
import uk.gov.hmcts.appregister.dto.read.FeeReportFilterDto;

public interface FeeReportService {
    void generateFeeReportCsv(FeeReportFilterDto filter, HttpServletResponse response)
            throws IOException;
}
