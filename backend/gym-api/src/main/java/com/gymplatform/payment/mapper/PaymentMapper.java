package com.gymplatform.payment.mapper;

import com.gymplatform.payment.domain.Payment;
import com.gymplatform.payment.dto.PaymentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);
}
