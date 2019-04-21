package com.demo.dao;

import com.demo.model.Person;

import java.util.Collection;
import java.util.List;

public interface PersonDAO {

    Collection<Person> findAll();

    Person getPersonById(Long id);

    void removePerson(Long id);

    Person save(final Person person);

    void save(final List<Person> people);

    Person update(final Person person);

}