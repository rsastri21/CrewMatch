package com.lux.crewmatch.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "HEADERS")
public class Header {

    // Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "NAME")
    private String name;

    @ElementCollection
    @CollectionTable(
            name = "HEADER_VALUES",
            joinColumns = @JoinColumn(name = "id", referencedColumnName = "id")
    )
    @Column(name = "CSV_HEADERS")
    private List<String> csvHeaders;

    public Header() {

    }

    // Getters and Setters

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

    public List<String> getCsvHeaders() {
        return csvHeaders;
    }

    public void setCsvHeaders(List<String> csvHeaders) {
        this.csvHeaders = csvHeaders;
    }
}
