package org.noses.homedefense.maps;

import org.noses.homedefense.users.AccountDTO;
import org.noses.homedefense.users.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.io.IOException;

@RestController
@RequestMapping("/maps")
public class MapsResource {
    @Autowired
    MapsImporter mapsImporter;

    @Autowired
    MapsReader mapsReader;
    @Autowired
    AccountService accountService;

    @GET
    @RequestMapping("/import/{city}")
    public void doImport(@PathVariable("city") String city) throws Exception {
        mapsImporter.doImport(city);
    }

    @GET
    @RequestMapping("/{width}/{height}/{north}/{west}/{south}/{east}")
    public ResponseEntity<MapDTO> getMap(@RequestHeader("X-Authorization-Token") String authorizationToken, @PathVariable("width") int width,
                       @PathVariable("height") int height,
                       @PathVariable("north") Float north,
                       @PathVariable("west") Float west,
                       @PathVariable("south") Float south,
                       @PathVariable("east") Float east) {

        if (StringUtils.isEmpty(authorizationToken)) {
            new ResponseEntity<String>("unauthorized", HttpStatus.UNAUTHORIZED);
        }

        AccountDTO accountDTO = accountService.getAccountByToken(authorizationToken);
        if (accountDTO == null) {
            return new ResponseEntity<MapDTO>(new MapDTO(), HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<MapDTO>(mapsReader.readMap(width, height, north, west, south, east), HttpStatus.OK);
    }

}
