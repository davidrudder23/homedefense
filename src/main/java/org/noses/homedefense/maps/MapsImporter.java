package org.noses.homedefense.maps;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;

@Component
@Slf4j
public class MapsImporter extends DefaultHandler {

    private int level;

    public void doImport() throws Exception {
        //FileInputStream fileInputStream =    new FileInputStream("/home/drig/Downloads/map");
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        xmlReader.setContentHandler(this);
        xmlReader.parse("/Users/drudder/Downloads/map");
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
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        level--;
        log.debug("end element qName={}", qName);
    }
}
