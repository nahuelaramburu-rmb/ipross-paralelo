package com.capacidad.validationapi.module.importprocessor.model;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ImportReport {

    private final AtomicInteger errors;
    private final AtomicInteger operations;
    private final AtomicLong duration;
    private final Set<String> errorList;

    public ImportReport() {
        this.errors = new AtomicInteger(0);
        this.operations = new AtomicInteger(0);
        this.duration = new AtomicLong(0L);
        this.errorList = new HashSet<>();
    }

    public void addError(String error) {
        this.errorList.add(error);
    }

    public void addAllErrors(Set<String> errors) {
        this.errorList.addAll(errors);
    }

    public Set<String> getErrorList() {
        return this.errorList;
    }

    public int getErrors() {
        return this.errors.get();
    }

    public int getOperations() {
        return this.operations.get();
    }

    public void setOperations(int operations) {
        this.operations.set(operations);
    }

    public void incrementErrors() {
        this.errors.incrementAndGet();
    }

    public void addOperations(int operations) {
        this.operations.addAndGet(operations);
    }

    public long getDuration() {
        return this.duration.get();
    }

    public void setDuration(long seconds) {
        this.duration.set(seconds);
    }

}
