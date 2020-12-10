package com.itmo.mse.soft.api.frontend.v1;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestBody;

@Data
@AllArgsConstructor
public class AuthData {
  String userName;
  String password;
}
