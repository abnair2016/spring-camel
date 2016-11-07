package com.demo.route;

import static com.demo.util.SpringCamelDemoUtil.QUESTION_MARK;
import static com.demo.util.SpringCamelDemoUtil.AMPERSAND;
import static com.demo.util.SpringCamelDemoUtil.COLON;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.bindy.csv.BindyCsvDataFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.demo.model.PersonCsvRecord;
import com.demo.service.PersonServiceImpl;
import com.demo.util.ArrayListAggregationStrategy;
import com.demo.util.BatchSizePredicate;
import com.demo.util.CsvRecordToPersonMapper;

@Component
public class SpringCamelRoute extends RouteBuilder {

    @Autowired
    private CsvRecordToPersonMapper mapper;
    
    @Autowired
    private PersonServiceImpl personService;
    
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
            .bean(personService)
            .to("bean:personService?method=findAll")
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