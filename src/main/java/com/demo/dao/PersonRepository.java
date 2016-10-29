package com.demo.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.demo.model.Person;
import com.demo.util.PersonRowMapper;

@Repository
public class PersonRepository {

    private static final Logger log = LoggerFactory.getLogger(PersonRepository.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Value("${batch.insert.sql}")
    private String batchInsertSQLQuery;
    
    @Value("${select.sql}")
    private String selectSQLQuery;
    
    @Transactional
    public void save(final List<Person> people) {
        
        jdbcTemplate.batchUpdate(batchInsertSQLQuery, new BatchPreparedStatementSetter() {
            
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Person person = people.get(i);
                ps.setString(1, person.getFirstName());
                ps.setString(2, person.getLastName());
                
            }
            
            @Override
            public int getBatchSize() {
                return people.size();
            }
        });
        log.info("Saved " + people.size() + " records ...");
    }
    
    @Transactional(readOnly=true)
    public List<Person> getPeople(){
        log.info("!!! JOB FINISHED! Time to verify the results", log);
        List<Person> persons = jdbcTemplate.query(selectSQLQuery, new PersonRowMapper());
        log.info("Found " + persons.size() + " people in database!");
        persons.forEach(person -> log.info("Found <" + person + "> in the database."));
        return persons;
    }
}