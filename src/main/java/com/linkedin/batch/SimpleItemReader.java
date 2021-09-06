package com.linkedin.batch;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleItemReader implements ItemReader<String> {

    private final Iterator<String> iterator;

    public SimpleItemReader() {
        List<String> dataSet = new ArrayList<>();
        dataSet.add("1");
        dataSet.add("2");
        dataSet.add("3");
        dataSet.add("4");
        dataSet.add("5");
        this.iterator = dataSet.iterator();
    }

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return iterator.hasNext() ? iterator.next(): null;
    }
}
