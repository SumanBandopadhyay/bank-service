package com.example.bankservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountTransferResponse {

    Double newSourceBalance;

    Double totalDestBalance;

    LocalDateTime timestamp;

}
