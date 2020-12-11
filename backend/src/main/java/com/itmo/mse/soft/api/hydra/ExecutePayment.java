package com.itmo.mse.soft.api.hydra;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExecutePayment {
  UUID paymentId;
  BigDecimal amount;
}
