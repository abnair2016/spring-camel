package com.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.demo.model.Person;
import com.demo.model.PersonCsvRecord;

@Component
public class CsvRecordToPersonMapper {
    
    private static Logger log = LoggerFactory.getLogger(CsvRecordToPersonMapper.class);
    
    public Person convertAndTransform(PersonCsvRecord csvRecord){
        Person person = new Person();
        person.setFirstName(csvRecord.getFirstName().trim().toUpperCase());
        person.setLastName(csvRecord.getLastName().trim().toUpperCase());
        log.info("Converting (" + csvRecord + ") into (" + person + ")", log);
        return person;
    }
}