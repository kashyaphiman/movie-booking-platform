package com.xyz.movieticket.strategy;

import com.xyz.movieticket.model.User;
import com.xyz.movieticket.model.enums.ShowType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PricingContext {
    private BigDecimal basePrice;
    private int numberOfSeats;
    private ShowType showType;
    private User user;
    private BigDecimal subtotal;
}