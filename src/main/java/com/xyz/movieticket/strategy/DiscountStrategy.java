package com.xyz.movieticket.strategy;

import java.math.BigDecimal;

public interface DiscountStrategy {
    boolean isApplicable(PricingContext context);
    BigDecimal calculateDiscount(PricingContext context);
    String getName();
}