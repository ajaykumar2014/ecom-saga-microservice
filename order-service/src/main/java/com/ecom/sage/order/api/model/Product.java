package com.ecom.sage.order.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {
    String productId;
    Integer quantity;
    String productDetails;
    BigDecimal pricePerUnit;
}
