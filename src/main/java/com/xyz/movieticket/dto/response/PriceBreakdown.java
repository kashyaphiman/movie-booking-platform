package com.xyz.movieticket.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PriceBreakdown {
    private BigDecimal subtotal;
    private BigDecimal totalDiscount;
    private BigDecimal finalAmount;
    private List<AppliedDiscount> appliedDiscounts;

    @Data
    @Builder
    public static class AppliedDiscount {
        private String discountName;
        private BigDecimal discountAmount;
    }
}