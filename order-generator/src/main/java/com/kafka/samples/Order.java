package com.kafka.samples;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Order{
    public String orderId;
    public float price;
    public String currency;
    public long orderTimestamp;
    public Date orderTime;

    @JsonGetter("order_id")
    public String getOrderId() {
        return orderId;
    }

    @JsonSetter("order_id")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonGetter("price")
    public float getPrice() {
        return price;
    }

    @JsonSetter("price")
    public void setPrice(float price) {
        this.price = price;
    }

    @JsonGetter("currency")
    public String getCurrency() {
        return currency;
    }

    @JsonSetter("currency")
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @JsonGetter("order_ts")
    public long getOrderTimestamp() {
        return orderTimestamp;
    }

    @JsonSetter("order_ts")
    public void setOrderTimestamp(long orderTimestamp) {
        this.orderTimestamp = orderTimestamp;
        this.orderTime = new Date(orderTimestamp);
    }

    @JsonGetter("order_time")
    public String getOrderTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        return dateFormat.format(this.orderTime);
    }

    @Override
    public String toString() {
        return "Order{" +
                "order_id=" + orderId +
                ", price='" + price + '\'' +
                ", currency=" + currency +
                ", order_ts='" + orderTimestamp + '\'' +
                ", order_time='" + getOrderTime() + '\'' +
                '}';
    }
}
