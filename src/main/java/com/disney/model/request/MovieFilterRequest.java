package com.disney.model.request;


public class MovieFilterRequest {

    private String title;

    private Long genre;

    private String order;

    public boolean isASC() {
        return this.order.compareToIgnoreCase("ASC") == 0;
    }
}
