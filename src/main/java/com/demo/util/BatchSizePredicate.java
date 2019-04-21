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