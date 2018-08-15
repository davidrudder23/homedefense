package org.noses.homedefense.maps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.GET;
import java.io.IOException;

@RestController
public class MapsResource {
    @Autowired
    MapsImporter mapsImporter;

    @GET
    public void doImport() throws IOException {
        mapsImporter.doImport();
    }
}
