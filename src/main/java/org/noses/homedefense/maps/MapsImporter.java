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
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MapsImporter extends DefaultHandler {

    @Autowired
    private MapsRepository mapsRepository;

    @Value("${maps.filename}")
    private String mapsFilename;

    private int level;

    private int order;

    private Way way;

    private long wayNodeId;

    private List<WayNode> wayNodes;

    public void doImport() throws Exception {
        InputSource in = new InputSource(new FileInputStream(mapsFilename));
        doImport(in);
    }

    public void doImport(InputSource in) throws Exception {
        mapsRepository.init();

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
        wayNodeId = 0;
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        log.debug("endDocument");

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        level++;
        log.debug("start element qName={} level={}", qName, level);

        if (qName.equalsIgnoreCase("node") && (1==2)) {
            Node node = new Node();
            node.getPoint().setId(parseLong(attributes.getValue("id")));
            node.getPoint().setLat(parseFloat(attributes.getValue("lat")));
            node.getPoint().setLon(parseFloat(attributes.getValue("lon")));

            mapsRepository.insertNode(node);
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
            order = 0;

            wayNodes = new ArrayList<>();

                //way.setName(attributes.getValue("way"));
        } else if (qName.equalsIgnoreCase("nd")) {  // waynode
            if (way != null) {
                WayNode wayNode = new WayNode();
                wayNode.setWay(way.getId());
                wayNode.getWayNodeKey().setId(wayNodeId++);
                wayNode.setOrderNum(order++);
                wayNode.setNode(parseLong(attributes.getValue("ref")));
                wayNodes.add(wayNode);
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
                } else if ("highway".equalsIgnoreCase(attributes.getValue("k"))) {
                    way.setHighway(attributes.getValue("v"));
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        level--;
        log.debug("end element qName={}", qName);

        if (qName.equalsIgnoreCase("way")) {
            if ((way.getHighway() != null) &&
                    (("trunk".equalsIgnoreCase(way.getHighway())) ||
                    ("primary".equalsIgnoreCase(way.getHighway())) ||
                    ("secondary".equalsIgnoreCase(way.getHighway())) ||
                    ("tertiary".equalsIgnoreCase(way.getHighway())) ||
                    ("residential".equalsIgnoreCase(way.getHighway())) ||
                    ("road".equalsIgnoreCase(way.getHighway())) ||
                    ("motorway".equalsIgnoreCase(way.getHighway())))) {

                // TODO: don't insert ways. They're not technically necessary, but in for
                // debugging purposes
                mapsRepository.insertWay(way);

                for (WayNode wayNode: wayNodes) {
                    Node node = mapsRepository.getNode(wayNode.getNode());
                    wayNode.getWayNodeKey().setLat(node.getPoint().getLat());
                    wayNode.getWayNodeKey().setLon(node.getPoint().getLon());

                    // Denormalize the data from _way_
                    wayNode.setName(way.getName());
                    wayNode.setLanes(way.getLanes());
                    wayNode.setHighway(way.getHighway());
                    wayNode.setMaxSpeed(way.getMaxSpeed());
                    wayNode.setOneWay(way.isOneWay());

                    mapsRepository.insertWayNode(wayNode);
                }
            }
            way = null;
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
}
