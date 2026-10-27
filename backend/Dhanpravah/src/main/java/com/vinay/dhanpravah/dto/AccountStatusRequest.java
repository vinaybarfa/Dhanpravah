package com.vinay.dhanpravah.dto;

import com.vinay.dhanpravah.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record AccountStatusRequest(@NotNull AccountStatus status) {
}
