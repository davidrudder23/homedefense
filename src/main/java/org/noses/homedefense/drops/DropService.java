package org.noses.homedefense.drops;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DropService {
    @Autowired
    DropRepository dropRepository;

    SecureRandom secureRandom = null;

    public List<DropDTO> getDrops(double north,
                                  double south,
                                  double east,
                                  double west) {
        List<DropDTO> dropDTOS = dropRepository.getDropsByGeo(north, west, south, east)
                .stream()
                .map(d->new DropDTO(d.getClassName(), d.getJson(), d.getPoint().getLat(), d.getPoint().getLon()))
                .collect(Collectors.toList());
        return dropDTOS;
    }

    public boolean insertDrop(String accountId, DropDTO dropDTO) {
        Drop drop = new Drop();
        drop.setAccountId(accountId);
        drop.setClassName(dropDTO.getClassName());
        drop.setJson(dropDTO.getJson());
        DropPoint dropPoint = new DropPoint();
        dropPoint.setLat(dropDTO.getLatitude());
        dropPoint.setLon(dropDTO.getLongitude());
        drop.setPoint(dropPoint);
        System.out.println("Inserting drop "+drop);
        dropRepository.save(drop);
        return true;
    }
}