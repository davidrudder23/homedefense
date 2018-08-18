package org.noses.homedefense.maps;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;

@Component
@Slf4j
public class MapsImporter extends DefaultHandler {

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private WayRepository wayRepository;

    @Value( "${maps.filename}" )
    private String mapsFilename;

    private int level;

    private int count;

    private Way way;

    CassandraOperations template;

    public void doImport() throws Exception {
        InputSource in = new InputSource(new FileInputStream(mapsFilename));
        doImport(in);
    }

    public void doImport(InputSource in) throws Exception {
        Cluster cluster = Cluster.builder().addContactPoints("localhost").build();
        Session session = cluster.connect("dev");

        template = new CassandraTemplate(session);

        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(this);
        xmlReader.parse(in);
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        log.debug("startDocument");
        level = 0;
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        log.debug("endDocument");

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        level ++;
        log.debug("start element qName={} level={}", qName, level);

        if (qName.equalsIgnoreCase("node")) {
            Node node = new Node();
            node.setId(parseLong(attributes.getValue("id")));
            node.setLat(parseFloat(attributes.getValue("lat")));
            node.setLon(parseFloat(attributes.getValue("lon")));
            template.insert(node);
            //log.info("count={}", count++);
        } else if (qName.equalsIgnoreCase("way")) {
            if (way != null) {
                log.error("Received a new way node, but the existing way is not null");
            }

            way = new Way();
            // set reasonable defaults
            way.setOneWay(false);
            way.setMaxSpeed(25);
            way.setLanes(1);
            way.setName("Unnamed Street");
            way.setId(parseLong(attributes.getValue("id")));
            //way.setName(attributes.getValue("way"));
        } else if (qName.equalsIgnoreCase("nd")) {
            if (way != null) {
                way.addNode(parseLong(attributes.getValue("ref")));
            }
        } else if (qName.equalsIgnoreCase("tag")) {
            if (way != null) {
                if ("name".equalsIgnoreCase(attributes.getValue("k"))) {
                    way.setName(attributes.getValue("v"));
                } else if ("lanes".equalsIgnoreCase(attributes.getValue("k"))) {
                    // lanes requires a little more processing. If it has a semi-colon in it,
                    // it means the road has a different number of lanes in each direction
                    // or, there is a commuter lane or something else weird
                    // we really don't care about that, so just choose the 1st one
                    String lanes = attributes.getValue("v");
                    if (lanes.contains(";")) {
                        lanes = lanes.substring(0, lanes.indexOf(";"));
                    }
                    way.setLanes(parseInt(lanes));
                } else if ("maxspeed".equalsIgnoreCase(attributes.getValue("k"))) {
                    way.setMaxSpeed(parseInt(attributes.getValue("v").replaceAll("[^\\d.]", "")));
                } else if ("oneway".equalsIgnoreCase(attributes.getValue("k"))) {
                    way.setOneWay("yes".equalsIgnoreCase(attributes.getValue("v")));
                }
            }
        }
    }

    int parseInt(String input) {
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            log.warn("Could not parse {} as an integer", input);
            return 0;
        }
    }

    long parseLong(String input) {
        try {
            return Long.parseLong(input);
        } catch (Exception e) {
            log.warn("Could not parse {} as a long", input);
            return 0;
        }
    }

    float parseFloat(String input) {
        try {
            return Float.parseFloat(input);
        } catch (Exception e) {
            log.warn("Could not parse {} as a float", input);
            return 0;
        }
    }



    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        level--;
        log.debug("end element qName={}", qName);

        if (qName.equalsIgnoreCase("way")) {
            template.insert(way);
            way = null;
        }
    }
}
