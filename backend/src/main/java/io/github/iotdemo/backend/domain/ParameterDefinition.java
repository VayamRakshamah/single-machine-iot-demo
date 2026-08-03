package io.github.iotdemo.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name="parameter_definition")
public class ParameterDefinition {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    public String code;
    @Column(name="display_name") public String displayName;
    public String unit;
    @Column(name="data_type") public String dataType;
    @Column(name="minimum_valid_value") public Double minimumValidValue;
    @Column(name="maximum_valid_value") public Double maximumValidValue;
    @Column(name="warning_low") public Double warningLow;
    @Column(name="warning_high") public Double warningHigh;
    @Column(name="critical_low") public Double criticalLow;
    @Column(name="critical_high") public Double criticalHigh;
    @Column(name="display_order") public int displayOrder;
    public boolean visible;
    @Column(name="chart_enabled") public boolean chartEnabled;
    @Column(name="decimal_places") public int decimalPlaces;
    protected ParameterDefinition() {}
}

