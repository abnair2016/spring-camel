package com.demo.dao;

import java.util.Collection;
import java.util.List;

import com.demo.model.Person;

public interface PersonDAO {

    Collection<Person> findAll();

    Person getPersonById(Long id);

    void removePerson(Long id);

    Person save(final Person person);
    
    void save(final List<Person> people);

    Person update(final Person person);

}