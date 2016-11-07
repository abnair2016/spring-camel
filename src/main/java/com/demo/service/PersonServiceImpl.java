package com.demo.service;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.demo.dao.PersonDAO;
import com.demo.model.Person;

@Service("personService")
public class PersonServiceImpl implements PersonService {

    @Autowired
    @Qualifier("hsqlrepo")
    private PersonDAO personDAO;
    
    @Override
    public Collection<Person> findAll() {
        return personDAO.findAll();
    }

    @Override
    public Person getPersonById(Long id) {
        return personDAO.getPersonById(id);
    }
    
    @Override
    public void removePerson(Long id) {
        personDAO.removePerson(id);
    }

    @Override
    public Person save(Person person) {
        return personDAO.save(person); 
    }

    @Override
    public void save(List<Person> people) {
        personDAO.save(people);        
    }
    
    @Override
    public Person update(Person person) {
        return personDAO.update(person); 
    }

}
