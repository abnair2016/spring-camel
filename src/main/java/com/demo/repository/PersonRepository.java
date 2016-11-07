package com.demo.repository;

import org.springframework.data.repository.CrudRepository;

import com.demo.model.Person;

public interface PersonRepository extends CrudRepository<Person, Long> {

}
