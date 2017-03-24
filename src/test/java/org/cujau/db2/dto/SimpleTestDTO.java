package org.cujau.db2.dto;

import java.math.BigDecimal;

public class SimpleTestDTO implements IdPrivateKeyDTO {

    long id;
    String name;
    boolean isUseful;
    String symbol;
    BigDecimal cash;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUseful() {
        return isUseful;
    }

    public void setUseful(boolean isUseful) {
        this.isUseful = isUseful;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String sym) {
        this.symbol = sym;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public void setCash(BigDecimal c) {
        this.cash = c;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((cash == null) ? 0 : cash.hashCode());
        result = (prime * result) + (int) (id ^ (id >>> 32));
        result = (prime * result) + (isUseful ? 1231 : 1237);
        result = (prime * result) + ((name == null) ? 0 : name.hashCode());
        result = (prime * result) + ((symbol == null) ? 0 : symbol.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SimpleTestDTO other = (SimpleTestDTO) obj;
        if (cash == null) {
            if (other.cash != null) {
                return false;
            }
        } else if (cash.compareTo(other.cash) != 0) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (isUseful != other.isUseful) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (symbol == null) {
            if (other.symbol != null) {
                return false;
            }
        } else if (!symbol.equals(other.symbol)) {
            return false;
        }
        return true;
    }

}
