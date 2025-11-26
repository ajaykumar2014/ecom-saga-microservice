package com.ecom.sage.common.events;

public interface ShippingEvent extends PaymentEvent,RequestEvent{
    String getOrderId();
    String getShippingId();
}
