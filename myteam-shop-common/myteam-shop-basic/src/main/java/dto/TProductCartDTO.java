package dto;

import com.qf.entity.TProduct;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TProductCartDTO implements Serializable {

    private TProduct tProduct;

    private Integer count;

    private Date updateTime;
}
