package com.trego.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VandorCartResponseDTO {

    private long userId;
    private long orderId;
    private List<CartResponseDTO> carts;

}