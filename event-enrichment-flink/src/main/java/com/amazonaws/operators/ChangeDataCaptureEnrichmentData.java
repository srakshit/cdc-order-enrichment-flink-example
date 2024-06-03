package com.amazonaws.operators;

import com.amazonaws.pojo.Order;
import com.amazonaws.pojo.Rate;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class ChangeDataCaptureEnrichmentData extends KeyedCoProcessFunction<String, Order, Rate, Order> {

    private MapState<String, List<Rate>> rateMapState = null;
    private Rate rate = new Rate();
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String RESET = "\033[0m";  // Text Reset

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        MapStateDescriptor<String,List<Rate>> mapStateDesc = new MapStateDescriptor<>(
                "rateRefData",
                BasicTypeInfo.STRING_TYPE_INFO,
                TypeInformation.of(new TypeHint<>() {})
        );

        rateMapState = getRuntimeContext().getMapState(mapStateDesc);
    }

    //processElement1 determines how Customer data needs to be handled
    @Override
    public void processElement1(Order order, KeyedCoProcessFunction<String, Order, Rate, Order>.Context ctx, Collector<Order> out) throws Exception {
        if (rateMapState.get(ctx.getCurrentKey()) != null) {
            List<Rate> rates = rateMapState.get(ctx.getCurrentKey());
            Rate rate;
            for (int i = rates.size() - 1; i >= 0; i--)
            {
                rate = rates.get(i);
                //System.out.println(String.format("order_id: %s, order_ts: %s, rate_ts: %s, order_ts>rate_ts: %s, currency: %s, rate: %s", order.getOrderId(), order.getOrderTimestamp(), rate.getUpdateTimestamp(), order.getOrderTimestamp() > rate.getUpdateTimestamp(), rate.getCurrency(), rate.getRate()));
                if (i < (rates.size() - 1)) System.out.println(String.format(YELLOW_BOLD +"Late Event Processed: " + RESET));

                if (order.getOrderTimestamp() > rate.getUpdateTimestamp()) {
                    order.setConversionRate(rate.getRate());
                    out.collect(order);
                    return;
                }
            }
        }
    }

    //processElement2 determines how Order data needs to be handled
    @Override
    public void processElement2(Rate rate, KeyedCoProcessFunction<String, Order, Rate, Order>.Context ctx, Collector<Order> out) throws Exception {
        List<Rate> rates = new ArrayList<>();
        if (rateMapState.get(ctx.getCurrentKey()) != null) {
            rates = rateMapState.get(ctx.getCurrentKey());
        }
        rates.add(rate);
        System.out.println(String.format(CYAN_BOLD +"Rate changed for %s to %s at %s" + RESET, rate.getCurrency(), rate.getRate(), rate.getUpdateTime()));
        rateMapState.put(ctx.getCurrentKey(), rates);
    }
}
