package org.noses.homedefense.drops;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DropDTO {

    public String className;
    public String json;
    private double latitude;
    private double longitude;
}
