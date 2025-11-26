package com.ecom.sage.common.events;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReservedEvent implements RequestEvent {
    private String orderId;
    private String productId;
    private Integer quantity;
    private boolean success;
    private String notes;
}
