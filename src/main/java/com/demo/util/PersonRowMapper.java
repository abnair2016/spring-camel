package com.demo.util;

import static com.demo.util.SpringCamelDemoUtil.FIRST_NAME;
import static com.demo.util.SpringCamelDemoUtil.LAST_NAME;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.demo.model.Person;

public class PersonRowMapper implements RowMapper<Person>{

    @Override
    public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
        Person person = new Person();
        person.setFirstName((String)rs.getString(FIRST_NAME));
        person.setLastName((String)rs.getString(LAST_NAME));
        return person;
    }
}