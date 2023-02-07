package com.lux.crewmatch.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "PRODUCTIONS")
public class Production {

    // Fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "NAME")
    private String name;

    @ElementCollection
    @CollectionTable(
            name = "PRODUCTION_ROLES",
            joinColumns = @JoinColumn(name = "id", referencedColumnName = "id")
    )
    @Column(name = "ROLES")
    private List<String> roles;

    @ElementCollection
    @CollectionTable(
            name = "PRODUCTION_MEMBERS",
            joinColumns = @JoinColumn(name = "id", referencedColumnName = "id")
    )
    @Column(name = "MEMBERS")
    private List<String> members;

    @Column(name = "PRODUCTION_LEAD")
    private String prodLead;

    public Production() {

    }

    // Place a candidate on the production
    // Returns true if the placement attempt was successful
    public boolean place(Candidate candidate, String intendedRole) {
        // Attempt to place based on the roles
        for (int i = 0; i < this.roles.size(); i++) {
            if (intendedRole.equals(roles.get(i)) && members.get(i).equals("")) {
                // String to store including pronouns
                String name = candidate.getName();
                if (candidate.getPronouns() != null) {
                    name = name + " (" + candidate.getPronouns() + ")";
                }
                // Add candidate
                members.set(i, name);
                return true;
            }
        }
        return false;
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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getProdLead() {
        return prodLead;
    }

    public void setProdLead(String prodLead) {
        this.prodLead = prodLead;
    }
}
