package com.demo.model;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.springframework.stereotype.Component;

@Component
@CsvRecord(separator=",", skipFirstLine=true, crlf="WINDOWS")
public class PersonCsvRecord {

    @DataField(pos=2, required=true, trim=true)
    private String lastName;
    @DataField(pos=1, required=false, trim=true, defaultValue=" ")
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
        return new StringBuilder().append("[CSV RECORD:: First Name: ")
                .append(this.firstName)
                .append("; Last Name: ")
                .append(this.lastName)
                .append("]")
                .toString();
    }
}