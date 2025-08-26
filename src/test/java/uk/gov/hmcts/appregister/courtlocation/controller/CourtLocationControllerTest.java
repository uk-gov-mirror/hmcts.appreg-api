package uk.gov.hmcts.appregister.courtlocation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.appregister.courtlocation.dto.CourtLocationDto;
import uk.gov.hmcts.appregister.courtlocation.service.CourtLocationService;

@ExtendWith(MockitoExtension.class)
class CourtLocationControllerTest {

    @Mock private CourtLocationService service;

    @InjectMocks private CourtLocationController controller;

    @Test
    void getAll_returnsOkWithBody() {
        CourtLocationDto dto1 = mock(CourtLocationDto.class);
        CourtLocationDto dto2 = mock(CourtLocationDto.class);
        when(service.findAll()).thenReturn(List.of(dto1, dto2));

        ResponseEntity<List<CourtLocationDto>> resp = controller.getAll();

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).containsExactly(dto1, dto2);
        verify(service).findAll();
    }

    @Test
    void getById_returnsOkWithBody() {
        CourtLocationDto dto = mock(CourtLocationDto.class);
        when(service.findById(123L)).thenReturn(dto);

        ResponseEntity<CourtLocationDto> resp = controller.getById(123L);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isSameAs(dto);
        verify(service).findById(123L);
    }
}
