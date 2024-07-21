package ite.project.hbase.commons.data.model.paging;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors( chain = true)
public class Order {
    private String property;
    private String direction;

    public enum Direction{
        asc,desc;
    }

}

