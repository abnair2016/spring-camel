package com.demo.dao;

import com.demo.model.Person;
import com.demo.repository.PersonRepository;
import com.demo.util.PersonRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
@Qualifier("hsqlrepo")
@Slf4j
@RequiredArgsConstructor
public class HsqlPersonDAOImpl implements PersonDAO {

    private final PersonRepository personRepository;

    private final JdbcTemplate jdbcTemplate;

    @Value("${batch.insert.sql}")
    private String batchInsertSQLQuery;

    @Value("${select.sql}")
    private String selectSQLQuery;

    @Override
    @Transactional(readOnly = true)
    public Collection<Person> findAll() {
        log.info("!!! JOB FINISHED! Time to verify the results");
        final List<Person> people = jdbcTemplate.query(selectSQLQuery, new PersonRowMapper());
        log.info("Found {} people in database!", people.size());
        people.forEach(person -> log.info("Found <{}> in the database.", person));
        return people;
    }

    @Override
    @Transactional(readOnly = true)
    public Person getPersonById(Long id) {
        return personRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public void removePerson(Long id) {
        personRepository.delete(this.getPersonById(id));
    }

    @Override
    public Person save(Person person) {
        return personRepository.save(person);
    }

    @Override
    @Transactional
    public void save(final List<Person> people) {

        jdbcTemplate.batchUpdate(batchInsertSQLQuery, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                final Person person = people.get(i);
                ps.setString(1, person.getFirstName());
                ps.setString(2, person.getLastName());

            }

            @Override
            public int getBatchSize() {
                return people.size();
            }
        });
        log.info("Saved {} records ...", people.size());
    }

    @Override
    @Transactional
    public Person update(Person person) {
        final Person persistedPerson = getPersonById(person.getId());
        if (persistedPerson == null) {
            return null;
        }

        return personRepository.save(person);
    }

}
