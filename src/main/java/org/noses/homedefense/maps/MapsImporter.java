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
            node.setId(Long.parseLong(attributes.getValue("id")));
            node.setLat(Float.parseFloat(attributes.getValue("lat")));
            node.setLon(Float.parseFloat(attributes.getValue("lon")));
            //nodeRepository.save(node);
            template.insert(node);
            //log.info("count={}", count++);
        } else if (qName.equalsIgnoreCase("way")) {
            if (way != null) {
                log.error("Received a new way node, but the existing way is not null");
            }

            way = new Way();
            way.setId(Long.parseLong(attributes.getValue("id")));
            //way.setName(attributes.getValue("way"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        level--;
        log.debug("end element qName={}", qName);

        if (qName.equalsIgnoreCase("way")) {
            way = null;
        }
    }
}
