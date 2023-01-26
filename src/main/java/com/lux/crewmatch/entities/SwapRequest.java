package com.lux.crewmatch.entities;

// This class defines the outline for a swap request to swap crew members between two productions.
public class SwapRequest {
    private String member1;
    private String role1;
    private String production1;

    private String member2;
    private String role2;
    private String production2;

    public String getMember1() {
        return member1;
    }

    public String getRole1() {
        return role1;
    }

    public String getProduction1() {
        return production1;
    }

    public String getMember2() {
        return member2;
    }

    public String getRole2() {
        return role2;
    }

    public String getProduction2() {
        return production2;
    }
}
