package nablarch.core.validation.convertor;

import nablarch.core.validation.PropertyName;

import java.math.BigDecimal;


public class TestTarget {
    private BigDecimal param;

    @PropertyName(messageId = "PROP0001")
    public void setParam(BigDecimal param) {
        this.param = param;
    }

    public BigDecimal getParam() {
        return param;
    }
}