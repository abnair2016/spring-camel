package com.demo.util;

import com.demo.model.Person;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PersonRowMapper implements RowMapper<Person> {
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";

    @Override
    public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Person.builder()
                .firstName(rs.getString(FIRST_NAME))
                .lastName(rs.getString(LAST_NAME))
                .build();
    }
}