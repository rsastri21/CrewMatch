package com.lux.crewmatch.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "CONFIGURATIONS")
public class Configs {

    // Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CONFIG_VALUE")
    private Integer value;

    public Configs () {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
