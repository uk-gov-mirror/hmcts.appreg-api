package uk.gov.hmcts.appregister.applicationentry.service;

import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.appregister.common.entity.ApplicationListEntry;
import uk.gov.hmcts.appregister.common.entity.repository.ApplicationListEntryRepository;
import uk.gov.hmcts.appregister.generated.model.ApplicationListStatus;
import uk.gov.hmcts.appregister.generated.model.EntryGetFilterDto;
import uk.gov.hmcts.appregister.generated.model.EntryPage;

import java.time.LocalDate;

public class ApplicationEntryServiceImpl implements ApplicationEntryService {

    private ApplicationListEntryRepository applicationListEntryRepository;

    @Override
    public EntryPage search(EntryGetFilterDto filterDto, Pageable pageable) {
        ApplicationListEntry entry = new ApplicationListEntry();

        applicationListEntryRepository
                .findApplicationList( filterDto.getDate(),
                        filterDto.getCourtName(),
                        filterDto.getOtherLocationDescription(),
                        filterDto.getCjaCode(),
                        filterDto.getApplicantOrganisation(),
                        filterDto.getApplicantSurname(),
                        filterDto.getStandardApplicantCode(),
                        filterDto.getStatus(),
                        filterDto.getRespondentOrganisation(),
                        filterDto.getRespondentSurname(),
                        filterDto.getRespondentPostcode(),
                        filterDto.getAccountReference(),
                        pageable
                        );

        return null;
    }
}
