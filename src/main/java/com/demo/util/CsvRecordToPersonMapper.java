package com.demo.util;

import com.demo.model.Person;
import com.demo.model.PersonCsvRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CsvRecordToPersonMapper {

    public Person convertAndTransform(PersonCsvRecord csvRecord) {
        final Person person = Person.builder()
                .firstName(csvRecord.getFirstName().trim().toUpperCase())
                .lastName(csvRecord.getLastName().trim().toUpperCase())
                .build();
        log.info("Converting ({}) into ({})", csvRecord, person);
        return person;
    }
}