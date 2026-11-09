package application.adapters.out.persistence.mysql.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "operations")
@Getter
@Setter
@NoArgsConstructor
public class OperationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "operation_id")
    private Integer operationId;

    @Column(name = "operation_type", nullable = false, length = 60)
    private String operationType;

    @Column(name = "execution_date", nullable = false)
    private LocalDateTime executionDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "performed_by_user_id")
    private UserJpaEntity performedBy;

    @Column(name = "affected_entity_type", length = 60)
    private String affectedEntityType;

    @Column(name = "affected_entity_id", length = 100)
    private String affectedEntityId;
}
