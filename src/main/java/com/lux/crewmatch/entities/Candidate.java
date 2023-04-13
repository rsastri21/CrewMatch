package com.lux.crewmatch.entities;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "CANDIDATES")
public class Candidate {

    // Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "PRONOUNS")
    private String pronouns;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "TIME")
    private String timestamp;

    @Column(name = "YEARS_IN_UW")
    private Integer yearsInUW;

    @Column(name = "QUARTERS_IN_LUX")
    private Integer quartersInLux;

    @Column(name = "ACTING_INTEREST")
    private Boolean actingInterest;

    @ElementCollection
    @CollectionTable(
            name = "CANDIDATE_PRODUCTIONS",
            joinColumns = @JoinColumn(name = "id", referencedColumnName = "id")
    )
    @Column(name = "PRODUCTIONS")
    private List<String> productions;

    @ElementCollection
    @CollectionTable(
            name = "CANDIDATE_ROLES",
            joinColumns = @JoinColumn(name = "id", referencedColumnName = "id")
    )
    @Column(name = "ROLES")
    private List<String> roles;

    @Column(name = "PROD_PRIORITY")
    private Boolean prodPriority;

    @Column(name = "ASSIGNED")
    private Boolean assigned;

    @Column(name = "PRODUCTION")
    private String production;

    @Column(name = "ROLE")
    private String role;

    public Candidate() {

    }

    // Getters and setters
    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPronouns() {
        return this.pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getYearsInUW() {
        return yearsInUW;
    }

    public void setYearsInUW(Integer yearsInUW) {
        this.yearsInUW = yearsInUW;
    }

    public Integer getQuartersInLux() {
        return quartersInLux;
    }

    public void setQuartersInLux(Integer quartersInLux) {
        this.quartersInLux = quartersInLux;
    }

    public Boolean getActingInterest() {
        return actingInterest;
    }

    public void setActingInterest(Boolean actingInterest) {
        this.actingInterest = actingInterest;
    }

    public List<String> getProductions() {
        return productions;
    }

    public void setProductions(List<String> productions) {
        this.productions = productions;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Boolean getProdPriority() {
        return prodPriority;
    }

    public void setProdPriority(Boolean prodPriority) {
        this.prodPriority = prodPriority;
    }

    public Boolean getAssigned() {
        return assigned;
    }

    public void setAssigned(Boolean assigned) {
        this.assigned = assigned;
    }

    public String getProduction() {
        return production;
    }

    public void setProduction(String production) {
        this.production = production;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
