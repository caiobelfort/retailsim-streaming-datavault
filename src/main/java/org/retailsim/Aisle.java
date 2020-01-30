package org.retailsim;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Aisle {
    public long id;
    public String name;
    public LocalDateTime create_at;
    public LocalDateTime updated_at;

}
