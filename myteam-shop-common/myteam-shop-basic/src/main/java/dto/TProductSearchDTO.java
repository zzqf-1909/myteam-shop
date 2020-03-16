package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TProductSearchDTO {

    private BigInteger id;
    private String tProductName;
    private BigDecimal tProductSalePrice;
    private String tProductPimage;
    private String tProductPdesc;

}
