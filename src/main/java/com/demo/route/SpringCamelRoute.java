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