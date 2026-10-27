package uk.gov.hmcts.appregister.csds.ingress;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CsdsIngestResponse {
    private Integer inserted;
    private Integer updated;
    private Integer endDated = 0;

    public CsdsIngestResponse inserted(Integer inserted) {
        this.inserted = inserted;
        return this;
    }

    public CsdsIngestResponse updated(Integer updated) {
        this.updated = updated;
        return this;
    }

    public CsdsIngestResponse endDated(Integer endDated) {
        this.endDated = endDated;
        return this;
    }
}
