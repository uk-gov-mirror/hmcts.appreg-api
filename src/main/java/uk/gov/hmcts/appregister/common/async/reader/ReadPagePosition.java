package uk.gov.hmcts.appregister.common.async.reader;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ReadPagePosition {
    int pageSize = 10;
    int startOffset;
}
