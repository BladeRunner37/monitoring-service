package org.example.monitoring.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.monitoring.model.ConsumptionType;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "consumption")
@Getter
@Setter
public class Consumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "measurement_id", referencedColumnName = "id")
    private Measurement measurement;

    private ConsumptionType type;

    private BigDecimal value;
}
