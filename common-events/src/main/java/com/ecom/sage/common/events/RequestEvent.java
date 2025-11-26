package com.ecom.sage.common.events;

import java.io.Serializable;

public interface RequestEvent extends Serializable {
    String getOrderId();
    String getNotes();
}
