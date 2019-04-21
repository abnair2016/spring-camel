# Apache Camel version of a Spring Batch Tutorial
A demo project that replicates a Spring Batch tutorial using Apache Camel within a Spring Boot app. 

[Get the Source Code from GitHub](https://github.com/abnair2016/spring-camel)

The purpose of this post is to replicate a specific use case that is listed in the [Spring Batch tutorial](https://spring.io/guides/gs/batch-processing), by reading CSV records and converting them into POJOs using Camel Bindy data format and applying [Enterprise Integration Patterns (EIPs)](http://www.enterpriseintegrationpatterns.com/patterns/messaging) like the Splitter and the Aggregator patterns to split, stream and batch insert records into and read results from an in-memory database respectively using Apache Camel route implementation within a Spring Boot application.

**Note:** Implementing this use case with the _camel-spring-batch_ component has been left out deliberately. If you are looking for the _camel-spring-batch_ component implementation, please check out the [Spring Boot demo application showing Camel and Spring Batch](https://github.com/gzurowski/camel-spring-batch-demo) authored by [Gregor Zurowski](https://github.com/gzurowski).

### Use-Case:
Build a service that: 

* Builds the file path from where data is read from
* Reads data from the CSV file
* Converts CSV records into POJO
* Transforms data with custom code into POJO
* Stores the final results in a database
* Reads back the stored results from the database
  
To resolve this use case, we would be using the following steps to create the Camel route:

1. Build the Input file URL using values specified in the _application.properties_ file
2. Use Transactions to ensure that if error occurs, the transaction is rolled back
3. Unmarshal the CSV data into POJO using Camel Bindy data format
4. Use the Splitter EIP to split and stream the messages
5. Use a Mapper bean to convert and transform the data
6. Use the Aggregator EIP to aggregate the POJOs into a List
7. Add a Predicate to limit the processing to a set number of records specified in _application.properties_
8. Wait for a timeout set in the _application.properties_ file
9. Use a bean to persist the list of records into the database as a batch
10. Finally, use a bean's method to retrieve the stored results from the database

### Splitter EIP:
![Splitter:](http://www.enterpriseintegrationpatterns.com/img/Sequencer.gif)

### Aggregator EIP:
![Aggregator:](http://www.enterpriseintegrationpatterns.com/img/Aggregator.gif)
  

### Versions used:
  * Spring Boot: 2.1.0.RELEASE
  * Apache Camel: 2.23.2
  * Maven: 3.0+
  * Java: 1.8

### Build with Maven:

#### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>com.demo.spring-camel</groupId>
	<artifactId>spring-camel</artifactId>
	<version>0.1.0</version>
	<packaging>jar</packaging>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.0.RELEASE</version>
		<relativePath/>
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
		<camel.version>2.23.2</camel.version>
		<lombok.version>1.18.4</lombok.version>
	</properties>

	<dependencies>
		<dependency>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-starter-batch</artifactId>
		</dependency>
		<dependency>
				<groupId>org.apache.camel</groupId>
				<artifactId>camel-spring-boot</artifactId>
				<version>${camel.version}</version>
		</dependency>
		<dependency>
				<groupId>org.apache.camel</groupId>
				<artifactId>camel-bindy</artifactId>
				<version>${camel.version}</version>
		</dependency>
		<dependency>
				<groupId>org.hsqldb</groupId>
				<artifactId>hsqldb</artifactId>
		</dependency>
		<dependency>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
				<groupId>org.projectlombok</groupId>
				<artifactId>lombok</artifactId>
				<version>${lombok.version}</version>
				<scope>provided</scope>
		</dependency>
        
         
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
</project>
```

### Resources

Following the Spring Batch tutorial, write a SQL script to create a table to store the data.

#### src/main/resources/schema-all.sql

```sql
DROP TABLE people IF EXISTS;

CREATE TABLE people  (
    person_id BIGINT IDENTITY NOT NULL PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);
```

Spring Boot runs schema-@@platform@@.sql automatically during startup.

**Business Data:**
Similar to the Spring Batch demo, the sample CSV data contains a first name and last name on each row, separated by a comma. 

#### src/main/resources/sample-data.csv

```csv
fname,lname
Jill,Doe
Joe,Doe
Justin,Doe
Jane,Doe
John,Doe
```

#### src/main/resources/application.properties

```properties
#### PROPERTIES FOR BATCH SIZE AND TIMEOUT ####
camel.batch.max.records=100
camel.batch.timeout=100

#### PROPERTIES TO BUILD FILE PATH ####
source.type=file
source.location=src/main/resources
noop.flag=true
recursive.flag=true
file.type=.*.csv

#### PROPERTIES TO BUILD SQL QUERIES ####
batch.insert.sql=INSERT INTO people(first_name, last_name) VALUES(?,?)
select.sql=SELECT * FROM people

spring.jpa.hibernate.ddl-auto=create
```

### Create a CsvRecord using Camel Bindy

The comma delimited format is handled by Apache Camel by unmarshalling using the Camel Bindy data format that loads each CSV row in the CSV file and map it to a _PersonCsvRecord_ POJO. The Csv Record skips the first line as the first line in the sample data in this demo is the title / column name.

#### src/main/java/com/demo/model/PersonCsvRecord.java

```java
package com.demo.model;

import lombok.Data;
import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.springframework.stereotype.Component;

@Component
@CsvRecord(separator = ",", skipFirstLine = true)
@Data
public class PersonCsvRecord {

    @DataField(pos = 2, required = true, trim = true)
    private String lastName;
    @DataField(pos = 1, trim = true, defaultValue = " ")
    private String firstName;

    @Override
    public String toString() {
        return "[CSV RECORD:: First Name: " + this.firstName +
                "; Last Name: " + this.lastName + "]";
    }
}
```

### Create a business class

_Person_ is a model class to represent a row that will be persisted to the database. 

#### src/main/java/com/demo/model/Person.java

```java
package com.demo.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "people")
@Data
@NoArgsConstructor
@Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 20)
    private String firstName;
    @Column(length = 20, nullable = false)
    private String lastName;

    public Person(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "[PERSON:: First Name: " + this.firstName +
                "; Last Name: " + this.lastName + "]";
    }
}
```

### Mapper

Each of the above models has a purpose, the former one to hold each CSV record read into a _CsvRecordPerson_ POJO to be further mapped and processed into the latter model that represents a Person row that finally persists to the database. The mapper bean is called to map the _CsvRecordPerson_ to _Person_ and convert the first and last names to upper case respectively.

#### src/main/java/com/demo/util/CsvRecordToPersonMapper.java

```java
package com.demo.util;

import com.demo.model.Person;
import com.demo.model.PersonCsvRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CsvRecordToPersonMapper {

    public Person convertAndTransform(PersonCsvRecord csvRecord) {
        final Person person = Person.builder()
                .firstName(csvRecord.getFirstName().trim().toUpperCase())
                .lastName(csvRecord.getLastName().trim().toUpperCase())
                .build();
        log.info("Converting ({}) into ({})", csvRecord, person);
        return person;
    }
}
```

### Apache Camel Route
_SpringCamelRoute_ is where the meat of the Camel route is defined for this use case.

#### src/main/java/com/demo/route/SpringCamelRoute.java

```java
package com.demo.route;

import com.demo.model.PersonCsvRecord;
import com.demo.service.PersonServiceImpl;
import com.demo.util.ArrayListAggregationStrategy;
import com.demo.util.BatchSizePredicate;
import com.demo.util.CsvRecordToPersonMapper;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringCamelRoute extends RouteBuilder {

    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String COLON = ":";

    private final CsvRecordToPersonMapper mapper;

    private final PersonServiceImpl personService;

    @Value("${camel.batch.timeout}")
    private long batchTimeout;

    @Value("${camel.batch.max.records}")
    private int maxRecords;

    @Value("${source.type}")
    private String sourceType;

    @Value("${source.location}")
    private String sourceLocation;

    @Value("${noop.flag}")
    private boolean isNoop;

    @Value("${recursive.flag}")
    private boolean isRecursive;

    @Value("${file.type}")
    private String fileType;

    @Override
    public void configure() {

        final BindyCsvDataFormat bindyCsvDataFormat = new BindyCsvDataFormat(PersonCsvRecord.class);
        bindyCsvDataFormat.setLocale("default");

        from(buildFileUrl())
                .transacted()
                .unmarshal(bindyCsvDataFormat)
                .split(body())
                .streaming()
                .bean(mapper, "convertAndTransform")
                .aggregate(constant(true), new ArrayListAggregationStrategy())
                .completionPredicate(new BatchSizePredicate(maxRecords))
                .completionTimeout(batchTimeout)
                .bean(personService)
                .to("bean:personService?method=findAll")
                .end();
    }

    private String buildFileUrl() {
        return sourceType + COLON + sourceLocation +
                QUESTION_MARK + "noop=" + isNoop +
                AMPERSAND + "recursive=" + isRecursive +
                AMPERSAND + "include=" + fileType;
    }
}
```

### Aggregator
_ArrayListAggregationStrategy_ is an Aggregation Strategy used for aggregating the model into a List of _Person_ objects.

#### src/main/java/com/demo/util/ArrayListAggregationStrategy.java

```java
package com.demo.util;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import java.util.ArrayList;
import java.util.List;

public class ArrayListAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        Object newBody = newExchange.getIn().getBody();
        List<Object> list;
        if (oldExchange == null) {
            list = new ArrayList<>();
            list.add(newBody);
            newExchange.getIn().setBody(list);
            return newExchange;
        } else {
            list = oldExchange.getIn().getBody(ArrayList.class);
            list.add(newBody);
            return oldExchange;
        }
    }
}
```

_BatchSizePredicate_ is a util class to limit the Batch size to a max number of records as specified in the _application.properties_ file.

#### src/main/java/com/demo/util/BatchSizePredicate.java

```java
package com.demo.util;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class BatchSizePredicate implements Predicate {

    private final int size;

    public BatchSizePredicate(int size) {
        this.size = size;
    }

    @Override
    public boolean matches(Exchange exchange) {
        if (exchange != null) {
            final List<Object> list = exchange.getIn().getBody(ArrayList.class);
            return !CollectionUtils.isEmpty(list) && list.size() == size;
        }
        return false;
    }

}
```

_PersonRowMapper_ is a util class to map each row from the resultset from the database to a _Person_ object.

#### src/main/java/com/demo/util/PersonRowMapper.java

```java
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
```

### Repository

_PersonRepository_ extends CrudRepository where the pre-defined database operations are used for this use case.

#### src/main/java/com/demo/repository/PersonRepository.java

```java
package com.demo.repository;

import com.demo.model.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, Long> {
}
```

### Data Access Objects

_PersonDAO_ is an interface exposed to the Service class

#### src/main/java/com/demo/dao/PersonDAO.java

```java
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
```

_HsqlPersonDAOImpl_ is the hsql implementation of the _PersonDAO_ interface. This approach potentially allows other DB implementations if required.

#### src/main/java/com/demo/dao/HsqlPersonDAOImpl.java

```java
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
```

### Service classes

The Service interface and implementation has been added to loosely couple the DB related implementations from the Camel route

#### src/main/java/com/demo/service/PersonService.java

```java
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
```

#### src/main/java/com/demo/service/PersonServiceImpl.java

```java
package com.demo.service;

import com.demo.dao.PersonDAO;
import com.demo.model.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service("personService")
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    @Qualifier("hsqlrepo")
    private final PersonDAO personDAO;

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
```

### Make the application executable

_SpringCamelApplication_ is the main executable class where everything is packaged in a single, executable JAR file, driven by a good old Java main() method.

#### src/main/java/com/demo/SpringCamelApplication.java

```java
package com.demo;

import org.apache.camel.spring.boot.CamelSpringBootApplicationController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SpringCamelApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(SpringCamelApplication.class, args);

        CamelSpringBootApplicationController applicationController = ctx.getBean(CamelSpringBootApplicationController.class);
        applicationController.run();

    }
}
```

### Output

The output is similar to the Spring Batch tutorial where each person is transformed and saved to the database and the query results from the database is output as well.

```output
Converting ([CSV RECORD:: First Name: Jill; Last Name: Doe]) into ([PERSON:: First Name: JILL; Last Name: DOE])
Converting ([CSV RECORD:: First Name: Joe; Last Name: Doe]) into ([PERSON:: First Name: JOE; Last Name: DOE])
Converting ([CSV RECORD:: First Name: Justin; Last Name: Doe]) into ([PERSON:: First Name: JUSTIN; Last Name: DOE])
Converting ([CSV RECORD:: First Name: Jane; Last Name: Doe]) into ([PERSON:: First Name: JANE; Last Name: DOE])
Converting ([CSV RECORD:: First Name: John; Last Name: Doe]) into ([PERSON:: First Name: JOHN; Last Name: DOE])
Saved 5 records ...
!!! JOB FINISHED! Time to verify the results
Found 5 people in database!
Found <[PERSON:: First Name: JILL; Last Name: DOE]> in the database.
Found <[PERSON:: First Name: JOE; Last Name: DOE]> in the database.
Found <[PERSON:: First Name: JUSTIN; Last Name: DOE]> in the database.
Found <[PERSON:: First Name: JANE; Last Name: DOE]> in the database.
Found <[PERSON:: First Name: JOHN; Last Name: DOE]> in the database.
```

### Summary

Congratulations! You built a batch job that ingested data from a spreadsheet, processed it, and wrote it to a database using Apache Camel within a Spring Boot Application.
