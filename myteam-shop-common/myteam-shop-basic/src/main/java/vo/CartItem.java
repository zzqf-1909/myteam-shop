package vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CartItem implements Serializable {

    private Integer productId;

    private Integer count;

    private Date updateTime;

}
