package io.github.iotdemo.backend.domain;

import jakarta.persistence.*;

@Entity
public class Machine {
    @Id public String id;
    public String name;
    @Column(name="plant_id") public String plantId;
    public String description;
    public boolean simulated;
    protected Machine() {}
}

