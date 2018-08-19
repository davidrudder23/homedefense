package org.noses.homedefense.maps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GET
    @RequestMapping("/import")
    public void doImport() throws Exception {
        mapsImporter.doImport();
    }

    @GET
    @RequestMapping("/{width}/{height}/{north}/{west}/{south}/{east}")
    public MapDTO getMap(@PathVariable("width") int width,
                       @PathVariable("height") int height,
                       @PathVariable("north") Float north,
                       @PathVariable("west") Float west,
                       @PathVariable("south") Float south,
                       @PathVariable("east") Float east) {

        return mapsReader.readMap(width, height, north, west, south, east);
    }

}
