package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "data_audit")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DataAudit {
    @Id
    @Column(name = "aler_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "add_dataaudit_event_gen")
    @SequenceGenerator(name = "add_dataaudit_event_gen", sequenceName = "add_dataaudit_event", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "schema", nullable = false)
    private String schema;

    @Column(name = "schema_name", nullable = false)
    private String schema_name;

    @Column(name = "table_name", nullable = false)
    private String table_name;

    @Column(name = "column_name", nullable = false)
    private String column_name;

    @Column(name = "old_value")
    private String old_value;

    @Column(name = "new_value")
    private String new_value;

    @Column(name = "user_id")
    private String user_id;

    @Column(name = "link")
    private String link;

    @Column(name = "created_date", nullable = false)
    private OffsetDateTime created_date;

    @Column(name = "old_clob_value")
    private String old_clob_value;

    @Column(name = "related_key")
    private Long related_key;

    @Column(name = "update_type", nullable = false)
    private String update_type;

    @Column(name = "data_type")
    private String data_type;

    @Column(name = "case_id")
    private Long case_id;

    @Column(name = "related_items_identifier")
    private String related_items_identifier;

    @Column(name = "related_items_identifier_index")
    private String related_items_identifier_index;

    @Column(name = "event_name")
    private String event_name;

    @Column(name = "user_name")
    private String user_name;
}
