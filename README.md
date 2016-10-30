# spring-camel
A demo project that replicates a Spring Batch tutorial using Apache Camel within a Spring Boot app.

## Self-learning Apache Camel through use-cases

The purpose of this post is to replicate a specific use case that is listed in the [Spring Batch tutorial](https://spring.io/guides/gs/batch-processing), by reading CSV records and converting them into POJOs using Camel Bindy data format and applying [Enterprise Integration Patterns (EIPs)](http://www.enterpriseintegrationpatterns.com/patterns/messaging) like the Splitter and the Aggregator patterns to split, stream and batch insert records into and read results from an in-memory database respectively using Apache Camel route implementation within a Spring Boot application.

### Use-Case:
Build a service that: 
	- Builds the file path from where data is read from
	- Reads data from the CSV file
	- Converts CSV records into POJO
	- Transforms data with custom code into POJO
	- Stores the final results in a database
	- Reads back the stored results from the database
  
To resolve this use case, we would be using the following steps to create the Camel route:
	1. Build the Input file URL using values specified in the _application.properties_ file
	2. Use Transactions to ensure that if error occurs, the transaction is rolled back
	3. Unmarshal the CSV data into POJO using Camel Bindy data format
	4. Use the Splitter EIP to split and stream the messages
  5. Use a Mapper bean to convert and transform the data
	6. Use the Aggregator EIP to aggregate the POJOs into a List
  7. Add a Predicate to limit the processing to a set number of records specified in the application.properties file
	8. Wait for a timeour set in the application.properties file
	9. Use a bean to persist the list of records into the database as a batch
	10. Finally, use a bean's method to retrieve the stored results from the database
  
### Splitter EIP:
![Splitter:](http://www.enterpriseintegrationpatterns.com/img/Sequencer.gif)

### Aggregator EIP:
![Aggregator:](http://www.enterpriseintegrationpatterns.com/img/Aggregator.gif)
  

### Versions used:
  * Spring Boot: 1.4.1.RELEASE
  * Apache Camel: 2.18.0
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
		<version>1.4.1.RELEASE</version>
		<relativePath/>
	</parent>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
        <camel.version>2.18.0</camel.version> 
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
            <groupId>commons-collections</groupId>
            <artifactId>commons-collections</artifactId>
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
```

### Create a CsvRecord using Camel Bindy

The comma delimited format is handled by Apache Camel by unmarshalling using the Camel Bindy data format that loads each CSV row in the CSV file and map it to a _PersonCsvRecord_ POJO. The Csv Record skips the first line as the first line in the sample data in this demo is the title / column name.

#### src/main/java/com/demo/model/PersonCsvRecord.java
```java
package com.demo.model;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import org.springframework.stereotype.Component;

@Component
@CsvRecord(separator=",", skipFirstLine=true, crlf="WINDOWS")
public class PersonCsvRecord {

    @DataField(pos=2, required=true, trim=true)
    private String lastName;
    @DataField(pos=1, required=false, trim=true, defaultValue=" ")
    private String firstName;
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("[CSV RECORD:: First Name: ")
                .append(this.firstName)
                .append("; Last Name: ")
                .append(this.lastName)
                .append("]")
                .toString();
    }
}
```

### Create a business class

_Person_ is a model class to represent a row that will be persisted to the database. 

#### src/main/java/com/demo/model/Person.java
```java
package com.demo.model;

public class Person {
    
    private String lastName;
    private String firstName;
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Override
    public String toString() {
        return new StringBuilder().append("[PERSON:: First Name: ")
                .append(this.firstName)
                .append("; Last Name: ")
                .append(this.lastName)
                .append("]")
                .toString();
    }
}
```

### Mapper

Each of the above models has a purpose, the former one to hold each CSV record read into a _CsvRecordPerson_ POJO to be further mapped and processed into the latter model that represents a Person row that finally persists to the database. The mapper bean is called to map the _CsvRecordPerson_ to _Person_ and convert the first and last names to upper case respectively.

#### src/main/java/com/demo/util/CsvRecordToPersonMapper.java
```java
package com.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.demo.model.Person;
import com.demo.model.PersonCsvRecord;

@Component
public class CsvRecordToPersonMapper {
    
    private static Logger log = LoggerFactory.getLogger(CsvRecordToPersonMapper.class);
    
    public Person convertAndTransform(PersonCsvRecord csvRecord){
        Person person = new Person();
        person.setFirstName(csvRecord.getFirstName().trim().toUpperCase());
        person.setLastName(csvRecord.getLastName().trim().toUpperCase());
        log.info("Converting (" + csvRecord + ") into (" + person + ")", log);
        return person;
    }
}
```

### Apache Camel Route
_SpringCamelRoute_ is where the meat of the Camel route is defined for this use case.

#### src/main/java/com/demo/route/SpringCamelRoute.java
```java
package com.demo.route;

import static com.demo.util.SpringCamelDemoUtil.QUESTION_MARK;
import static com.demo.util.SpringCamelDemoUtil.AMPERSAND;
import static com.demo.util.SpringCamelDemoUtil.COLON;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.demo.dao.PersonRepository;
import com.demo.model.PersonCsvRecord;
import com.demo.util.ArrayListAggregationStrategy;
import com.demo.util.BatchSizePredicate;
import com.demo.util.CsvRecordToPersonMapper;

@Component
public class SpringCamelRoute extends RouteBuilder {

    @Autowired
    private CsvRecordToPersonMapper mapper;
    
    @Autowired
    private PersonRepository personRepository;
    
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
    public void configure() throws Exception {
        
        BindyCsvDataFormat bindyCsvDataFormat = new BindyCsvDataFormat(PersonCsvRecord.class);
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
            .bean(personRepository)
            .to("bean:personRepository?method=getPeople")
            .end();
        
    }
    
    private String buildFileUrl(){
        StringBuilder fileUrlBuilder = new StringBuilder();
        return fileUrlBuilder.append(sourceType)
                        .append(COLON)
                        .append(sourceLocation)
                        .append(QUESTION_MARK)
                        .append("noop=")
                        .append(isNoop)
                        .append(AMPERSAND)
                        .append("recursive=")
                        .append(isRecursive)
                        .append(AMPERSAND)
                        .append("include=")
                        .append(fileType)
                        .toString();
    }
}
```

### Aggregator
_ArrayListAggregationStrategy_ is an Aggregation Strategy used for aggregating the model into a List of _Person_ objects.

#### src/main/java/com/demo/util/ArrayListAggregationStrategy.java
```java
package com.demo.util;

import java.util.ArrayList;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class ArrayListAggregationStrategy implements AggregationStrategy {
    
    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        Object newBody = newExchange.getIn().getBody();
        ArrayList<Object> list = null;
        if (oldExchange == null) {
                list = new ArrayList<Object>();
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

### Util classes

_SpringCamelDemoUtil_ is a final util class to hold the project constants in one place.

#### src/main/java/com/demo/util/SpringCamelDemoUtil.java
```java
package com.demo.util;

public final class SpringCamelDemoUtil {
    
    private SpringCamelDemoUtil() {
        //restrict instantiation
    }
    
    public static final String QUESTION_MARK = "?";
    public static final String AMPERSAND = "&";
    public static final String COLON = ":";
    
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    
}
```

_BatchSizePredicate_ is a util class to limit the Batch size to a max number of records as specified in the _application.properties_ file.

#### src/main/java/com/demo/util/BatchSizePredicate.java
```java
package com.demo.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.commons.collections.CollectionUtils;

public class BatchSizePredicate implements Predicate {

    public int size;

    public BatchSizePredicate(int size) {
        this.size = size;
    }
    
    @Override
    public boolean matches(Exchange exchange) {
        if (exchange != null) {
            List<Object> list = exchange.getIn().getBody(ArrayList.class);
            if (CollectionUtils.isNotEmpty(list) && list.size() == size) {
                return true;
            }
        }
        return false;
    }

}
```

_PersonRowMapper_ is a util class to map each row from the resulset from the database to a _Person_ object.

#### src/main/java/com/demo/util/PersonRowMapper.java
```java
package com.demo.util;

import static com.demo.util.SpringCamelDemoUtil.FIRST_NAME;
import static com.demo.util.SpringCamelDemoUtil.LAST_NAME;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.demo.model.Person;

public class PersonRowMapper implements RowMapper<Person>{

    @Override
    public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
        Person person = new Person();
        person.setFirstName((String)rs.getString(FIRST_NAME));
        person.setLastName((String)rs.getString(LAST_NAME));
        return person;
    }
}
```

### Repository

_PersonRepository_ is where the database operations are defined for the use case.

#### src/main/java/com/demo/dao/PersonRepository.java
```java
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
