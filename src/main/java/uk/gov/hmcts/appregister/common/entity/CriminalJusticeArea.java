package uk.gov.hmcts.appregister.common.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "criminal_justice_area")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CriminalJusticeArea {
    @Id
    @Column(name = "cja_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cja_gen")
    @SequenceGenerator(name = "cja_gen", sequenceName = "cja_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "cja_code", nullable = false)
    private String cjaCode;

    @Column(name = "cja_description", nullable = false)
    private String cjaDescription;
}
