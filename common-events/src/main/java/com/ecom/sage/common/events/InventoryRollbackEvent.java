package com.ecom.sage.common.events;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRollbackEvent implements RequestEvent {
    private String orderId;
    private String productId;
    private String notes;
}
