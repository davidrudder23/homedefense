package org.noses.homedefense.maps;

import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import org.xml.sax.InputSource;

public class MapsImporterTest {

    @Test
    @Ignore
    public void doImportTest() throws Exception {
        InputSource in = mock(InputSource.class);
        new MapsImporter().doImport(in);
    }
}
