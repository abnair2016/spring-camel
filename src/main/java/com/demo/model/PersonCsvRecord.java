package com.demo.model;

import lombok.Data;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.springframework.stereotype.Component;

@Component
@CsvRecord(separator = ",", skipFirstLine = true)
@Data
public class PersonCsvRecord {

    @DataField(pos = 2, required = true, trim = true)
    private String lastName;
    @DataField(pos = 1, trim = true, defaultValue = " ")
    private String firstName;

    @Override
    public String toString() {
        return "[CSV RECORD:: First Name: " + this.firstName +
                "; Last Name: " + this.lastName + "]";
    }
}