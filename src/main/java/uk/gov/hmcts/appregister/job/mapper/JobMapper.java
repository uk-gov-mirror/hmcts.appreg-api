package uk.gov.hmcts.appregister.job.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import uk.gov.hmcts.appregister.common.async.model.JobStatusResponse;
import uk.gov.hmcts.appregister.generated.model.JobAcknowledgement;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface JobMapper {
    @Mapping(target = "status", source = "jobStatusResponse.status")
    @Mapping(target = "id", source = "jobStatusResponse.uuid")
    @Mapping(target = "type", source = "jobStatusResponse.type")
    @Mapping(target = "errorDescription", source = "jobStatusResponse.errorMessage")
    JobAcknowledgement toDto(JobStatusResponse jobStatusResponse);
}
