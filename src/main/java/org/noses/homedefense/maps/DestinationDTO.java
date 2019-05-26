package org.noses.homedefense.maps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DestinationDTO {

    String id;

    double lat;
    double lon;

    String name;
    String description;

    public DestinationDTO(Destination destination) {
        this.id = destination.getKey().getId();
        this.lat = destination.getLat();
        this.lon = destination.getLon();
        this.name = destination.getName();
        this.description = destination.getDescription();
    }

}
