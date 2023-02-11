package com.lux.crewmatch.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "SWAP_REQUESTS")
public class SwapRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "COMPLETED")
    private Boolean completed;

    @Column(name = "LEAD_FROM")
    private String fromLead;

    @Column(name = "LEAD_TO")
    private String toLead;

    @Column(name = "PRODUCTION1")
    private String production1;

    @Column(name = "MEMBER1")
    private String member1;

    @Column(name = "ROLE1")
    private String role1;

    @Column(name = "PRODUCTION2")
    private String production2;

    @Column(name = "MEMBER2")
    private String member2;

    @Column(name = "ROLE2")
    private String role2;

    public SwapRequest() {

    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public String getFromLead() {
        return fromLead;
    }

    public void setFromLead(String fromLead) {
        this.fromLead = fromLead;
    }

    public String getToLead() {
        return toLead;
    }

    public void setToLead(String toLead) {
        this.toLead = toLead;
    }

    public String getProduction1() {
        return production1;
    }

    public void setProduction1(String production1) {
        this.production1 = production1;
    }

    public String getMember1() {
        return member1;
    }

    public void setMember1(String member1) {
        this.member1 = member1;
    }

    public String getRole1() {
        return role1;
    }

    public void setRole1(String role1) {
        this.role1 = role1;
    }

    public String getProduction2() {
        return production2;
    }

    public void setProduction2(String production2) {
        this.production2 = production2;
    }

    public String getMember2() {
        return member2;
    }

    public void setMember2(String member2) {
        this.member2 = member2;
    }

    public String getRole2() {
        return role2;
    }

    public void setRole2(String role2) {
        this.role2 = role2;
    }
}
