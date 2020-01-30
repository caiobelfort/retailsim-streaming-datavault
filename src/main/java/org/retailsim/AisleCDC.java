package org.retailsim;

import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class AisleCDC {
    public Aisle before;
    public Aisle after;
    public String op;
    public LocalDateTime timestamp;
}
