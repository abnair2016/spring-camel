package com.demo.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.demo.model.Person;
import com.demo.repository.PersonRepository;
import com.demo.util.PersonRowMapper;

@Repository
@Qualifier("hsqlrepo")
public class HsqlPersonDAOImpl implements PersonDAO {

    private static final Logger log = LoggerFactory.getLogger(HsqlPersonDAOImpl.class);

    @Autowired
    private PersonRepository personRepository;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Value("${batch.insert.sql}")
    private String batchInsertSQLQuery;
    
    @Value("${select.sql}")
    private String selectSQLQuery;
    
    @Override
    @Transactional(readOnly=true)
    public Collection<Person> findAll() {
        log.info("!!! JOB FINISHED! Time to verify the results", log);
        List<Person> people = jdbcTemplate.query(selectSQLQuery, new PersonRowMapper());
        log.info("Found " + people.size() + " people in database!");
        people.forEach(person -> log.info("Found <" + person + "> in the database."));
        return people;
    }
    
    @Override
    @Transactional(readOnly=true)
    public Person getPersonById(Long id){
        return personRepository.findOne(id);
    }
    
    @Override
    public void removePerson(Long id){
        personRepository.delete(id);
    }
    
    @Override
    public Person save(Person person) {
        Person savedPerson = personRepository.save(person);
        return savedPerson;
    }
    
    @Override
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

    @Override
    public Person update(Person person) {
        Person persistedPerson = getPersonById(person.getId());
        if(persistedPerson == null){
            return null;
        }
        
        Person updatedPerson = personRepository.save(person);
        return updatedPerson;
    }

}
