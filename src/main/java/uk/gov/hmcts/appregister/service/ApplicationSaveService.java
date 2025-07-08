package uk.gov.hmcts.appregister.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.appregister.model.Application;
import uk.gov.hmcts.appregister.repository.ApplicationRepository;

@Service
@RequiredArgsConstructor
public class ApplicationSaveService {

    private final ApplicationRepository repository;

    @Transactional
    public void saveApplication(Application app) {
        repository.save(app);
    }
}
