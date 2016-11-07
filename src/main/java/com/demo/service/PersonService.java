package com.demo.service;

import java.util.Collection;
import java.util.List;

import com.demo.model.Person;

public interface PersonService {
    
    Collection<Person> findAll();
    
    Person getPersonById(Long id);

    void removePerson(Long id);
    
    Person save(Person person);

    void save(List<Person> people);
    
    Person update(Person person);

}
