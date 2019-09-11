package org.noses.homedefense.drops;

import org.noses.homedefense.users.AccountDTO;
import org.noses.homedefense.users.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/drops")
public class DropResource {

    @Autowired
    DropService dropService;

    @Autowired
    AccountService accountService;

    @GET
    @RequestMapping(value = "/{north}/{south}/{east}/{west}", method = RequestMethod.GET)
    public ResponseEntity<List<DropDTO>> getDrops(@RequestHeader("X-Authorization-Token") String authorizationToken,
                                                  @PathVariable("north") double north,
                                                  @PathVariable("south") double south,
                                                  @PathVariable("east") double east,
                                                  @PathVariable("west") double west) {
        if (StringUtils.isEmpty(authorizationToken)) {
            new ResponseEntity<String>("unauthorized", HttpStatus.UNAUTHORIZED);
        }
        AccountDTO acccountDTO = accountService.getAccountByToken(authorizationToken);
        if (acccountDTO == null) {
            return new ResponseEntity<List<DropDTO>>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
        }

        List<DropDTO> drops = dropService.getDrops(north, south, east, west);
        return new ResponseEntity<List<DropDTO>>(drops, HttpStatus.OK);
    }

    @POST
    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<Boolean> insertDrop(@RequestHeader("X-Authorization-Token") String authorizationToken,
                              @RequestBody DropDTO drop) {
        if (StringUtils.isEmpty(authorizationToken)) {
            System.out.println("token is empty "+authorizationToken);
            new ResponseEntity<Boolean>(false, HttpStatus.UNAUTHORIZED);
        }
        AccountDTO acccountDTO = accountService.getAccountByToken(authorizationToken);
        if (acccountDTO == null) {
            System.out.println("Could not load account with token "+authorizationToken);
            return new ResponseEntity<Boolean>(false, HttpStatus.UNAUTHORIZED);
        }

        dropService.insertDrop(acccountDTO.getUsername(), drop);
        return new ResponseEntity<Boolean>(true, HttpStatus.OK);
    }
}
