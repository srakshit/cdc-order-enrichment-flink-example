package com.pojo;

import com.fasterxml.jackson.annotation.*;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Rate implements Serializable {
    private String currency;
    private float rate;
    private Date updateTime;
    private long updateTimestamp;

    public Rate() {}

    @JsonGetter("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonSetter("currency")
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonGetter("rate")
    public float getRate() {
        return rate;
    }

    @JsonGetter("last_update_ts")
    public Date getUpdateTime() {
        return updateTime;
    }

    @JsonSetter("last_update_ts")
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    @JsonProperty("after")
    private void latestRates(Map<String, String> rates) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

        this.currency = rates.get("currency");
        this.rate = Float.parseFloat(rates.get("rate"));
        this.updateTime = format.parse(rates.get("last_update_ts"));
        this.updateTimestamp = this.updateTime.getTime();
    }

    @Override
    public String toString() {
        return "Rate{" +
                "currency='" + currency + '\'' +
                ", rate='" + rate + '\'' +
                ", update_time='" + updateTime + '\'' +
                ", updateTimestamp='" + updateTimestamp + '\'' +
                '}';
    }
}
