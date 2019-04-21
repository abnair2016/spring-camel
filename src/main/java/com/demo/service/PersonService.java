package com.demo.service;

import com.demo.model.Person;

import java.util.Collection;
import java.util.List;

public interface PersonService {

    Collection<Person> findAll();

    Person getPersonById(Long id);

    void removePerson(Long id);

    Person save(Person person);

    void save(List<Person> people);

    Person update(Person person);

}
