package com.lux.crewmatch.entities;

public class Assignment {

    // Fields
    private Candidate candidate;
    private Production production;
    private Integer assignmentIndex;
    private String role;
    private Double weight;

    // Constructor
    public Assignment(Candidate candidate, Production production, Integer assignmentIndex, String role, Double weight) {
        this.candidate = candidate;
        this.production = production;
        this.assignmentIndex = assignmentIndex;
        this.role = role;
        this.weight = weight;
    }

    // Getters and Setters
    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Production getProduction() {
        return production;
    }

    public void setProduction(Production production) {
        this.production = production;
    }

    public Integer getAssignmentIndex() {
        return assignmentIndex;
    }

    public void setAssignmentIndex(Integer assignmentIndex) {
        this.assignmentIndex = assignmentIndex;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
