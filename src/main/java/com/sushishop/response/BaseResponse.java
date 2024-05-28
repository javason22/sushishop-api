package com.sushishop.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base response
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "BaseResponse", description = "Base response")
public class BaseResponse {
    
    public static final int NORMAL_CODE = 0;
    public static final int ERROR_CODE = 1;

    @Schema(name = "code", description = "Response code")
    protected int code;
    @Schema(name = "msg", description = "Response message")
    protected String msg;

    
}
