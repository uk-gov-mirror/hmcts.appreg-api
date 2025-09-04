package uk.gov.hmcts.appregister.applicationcode.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record ApplicationCodeDto(
        Long id,
        String applicationCode,
        String title,
        String wording,
        String legislation,
        Boolean feeDue,
        Boolean requiresRespondent,
        String destinationEmail1,
        String destinationEmail2,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        Boolean bulkRespondentAllowed,
        String feeReference,
        String mainFeeDescription,
        Double mainFeeAmount,
        String offsetFeeDescription,
        Double offsetFeeAmount) {}
