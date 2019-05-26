package org.noses.homedefense.maps;

import org.noses.homedefense.users.AccountDTO;
import org.noses.homedefense.users.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.GET;
import java.util.List;

@RestController
@RequestMapping("/maps")
public class MapsResource {
    @Autowired
    MapsImporter mapsImporter;

    @Autowired
    MapsService mapsService;
    @Autowired
    AccountService accountService;

    @GET
    @RequestMapping(value="/import/{city}", method= RequestMethod.GET)
    public void doImport(@PathVariable("city") String city) throws Exception {
        mapsImporter.doImport(city);
    }

    @GET
    @RequestMapping(value="/{width}/{height}", method= RequestMethod.GET)
    public ResponseEntity<MapDTO> getMap(@RequestHeader("X-Authorization-Token") String authorizationToken, @PathVariable("width") int width,
                       @PathVariable("height") int height) {

        if (StringUtils.isEmpty(authorizationToken)) {
            new ResponseEntity<String>("unauthorized", HttpStatus.UNAUTHORIZED);
        }

        AccountDTO accountDTO = accountService.getAccountByToken(authorizationToken);
        if (accountDTO == null) {
            return new ResponseEntity<MapDTO>(new MapDTO(), HttpStatus.UNAUTHORIZED);
        }

        float north = (float)(accountDTO.getHomeLongitude()+0.0075);
        float west = (float)(accountDTO.getHomeLatitude()-0.0150);
        float south = (float)(accountDTO.getHomeLongitude()-0.0075);
        float east = (float)(accountDTO.getHomeLatitude()+0.0150);

        return new ResponseEntity<MapDTO>(mapsService.readMap(width, height, north, west, south, east), HttpStatus.OK);
    }

    @GET
    @RequestMapping(value="/destinations", method=RequestMethod.GET)
    public ResponseEntity<List<DestinationDTO>> getDestinations() {
        return new ResponseEntity<>(mapsService.getDestinations(), HttpStatus.OK);
    }

}
