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
    @RequestMapping(value = "/import/{city}", method = RequestMethod.GET)
    public void doImport(@PathVariable("city") String city) throws Exception {
        mapsImporter.doImport(city);
    }

    @GET
    @RequestMapping(value = "/{north}/{south}/{east}/{west}", method = RequestMethod.GET)
    public ResponseEntity<MapDTO> getMap(@RequestHeader("X-Authorization-Token") String authorizationToken,
                                         @PathVariable("north") double north,
                                         @PathVariable("south") double south,
                                         @PathVariable("east") double east,
                                         @PathVariable("west") double west) {

        /*if (StringUtils.isEmpty(authorizationToken)) {
            new ResponseEntity<String>("unauthorized", HttpStatus.UNAUTHORIZED);
        }

        AccountDTO accountDTO = accountService.getAccountByToken(authorizationToken);
        if (accountDTO == null) {
            return new ResponseEntity<MapDTO>(new MapDTO(), HttpStatus.UNAUTHORIZED);
        }*/

        return new ResponseEntity<MapDTO>(mapsService.readMap(north, south, east, west), HttpStatus.OK);
    }

    @GET
    @RequestMapping(value = "/destinations", method = RequestMethod.GET)
    public ResponseEntity<List<DestinationDTO>> getDestinations() {
        return new ResponseEntity<>(mapsService.getDestinations(), HttpStatus.OK);
    }

}
