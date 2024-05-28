package com.sushishop.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "Request to place an order")
@Data
public class OrderRequest {

    @Schema(description = "Sushi name", example = "California Roll")
    @JsonProperty("sushi_name")
    @NotNull(message = "Sushi name is required")
    @NotBlank(message = "Sushi name is required")
    private String sushiName;

}
