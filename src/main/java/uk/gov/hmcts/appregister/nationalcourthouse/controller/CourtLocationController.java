package uk.gov.hmcts.appregister.nationalcourthouse.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import uk.gov.hmcts.appregister.common.security.RoleNames;
import uk.gov.hmcts.appregister.generated.api.CourtLocationApi;

@RequiredArgsConstructor
@Controller
public class CourtLocationController implements CourtLocationApi {
    private final CourtLocationService courtLocationService;

    @Override
    @PreAuthorize(RoleNames.USER_ROLE_OR_ADMIN_ROLE_RESTRICTION)

}
