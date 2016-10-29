package com.demo.model;

public class Person {
    
    private String lastName;
    private String firstName;
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append("[PERSON:: First Name: ")
                .append(this.firstName)
                .append("; Last Name: ")
                .append(this.lastName)
                .append("]")
                .toString();
    }
}