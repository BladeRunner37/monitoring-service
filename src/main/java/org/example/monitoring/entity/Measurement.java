package org.example.monitoring.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Table(name = "measurement")
@NamedEntityGraph(name = "Measurement.consumptions",
    attributeNodes = @NamedAttributeNode("consumptions"))
@Getter
@Setter
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "user_login", referencedColumnName = "login")
    private User user;

    @Column(name = "date_saved")
    private OffsetDateTime dateSaved;

    @OneToMany(mappedBy = "measurement", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Set<Consumption> consumptions;
}
