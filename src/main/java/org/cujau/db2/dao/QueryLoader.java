package org.cujau.db2.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.cujau.db2.DAOInitializationException;
import org.cujau.utils.StringUtil;
import org.cujau.xml.ThreadLocalValidator;

public class QueryLoader {

    public static final String CREATE_QUERY_NAME = "createQuery";
    public static final String DROP_QUERY_NAME = "dropQuery";

    private static final String NAMESPACE_URI = "http://org.cujau/db/dao/DAO-XML";

    static DocumentBuilder docParser;
    static ThreadLocalValidator docValidators;
    private static boolean setupComplete = false;
    private static boolean useDocuementBuilderReset = true;

    public static void setUseDocuementBuilderReset(boolean val) {
        useDocuementBuilderReset = val;
    }

    static void setup()
            throws ParserConfigurationException {
        if (setupComplete && useDocuementBuilderReset) {
            docParser.reset();
            return;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // You need this line since the document uses namespaces!
        factory.setNamespaceAware(true);
        docParser = factory.newDocumentBuilder();

        setupComplete = true;
    }

    public static Map<String, String> loadQueries(Class<?> klass, Properties dialectProps)
            throws ParserConfigurationException, SAXException, IOException, DAOInitializationException {
        setup();

        // Build the resource name to retrieve.
        String resName = klass.getName().replace('.', '/');
        resName = "/" + resName + ".xsql";

        Document queryDoc = null;
        InputStream stream = klass.getResourceAsStream(resName);
        if (stream == null) {
            throw new RuntimeException("Can't find xsql file: " + resName);
        }
        InputStreamReader reader = new InputStreamReader(stream);
        queryDoc = docParser.parse(new InputSource(reader));

        // Could save a few milliseconds by not validating the XSQL documents.
        //validateDocument( queryDoc );

        Map<String, String> queryMap = new HashMap<String, String>();

        // Get the ceate query.
        NodeList nodes = queryDoc.getElementsByTagNameNS(NAMESPACE_URI, "createQuery");

        if (nodes.getLength() > 0) {
            Element elem = (Element) nodes.item(0);
            String cQuery = elem.getTextContent().trim();
            queryMap.put(CREATE_QUERY_NAME, StringUtil.replaceProperties(cQuery, dialectProps));
        }

        // Get the drop query.
        nodes = queryDoc.getElementsByTagNameNS(NAMESPACE_URI, "dropQuery");

        if (nodes.getLength() > 0) {
            Element elem = (Element) nodes.item(0);
            String dQuery = elem.getTextContent().trim();
            queryMap.put(DROP_QUERY_NAME, StringUtil.replaceProperties(dQuery, dialectProps));
        }

        // Get the other queries.
        NodeList queries = queryDoc.getElementsByTagNameNS(NAMESPACE_URI, "query");
        for (int i = 0; i < queries.getLength(); i++) {
            Element item = (Element) queries.item(i);
            String id = item.getAttribute("id");
            String value = item.getTextContent().trim();
            queryMap.put(id, StringUtil.replaceProperties(value, dialectProps));
        }

        return queryMap;
    }

    /**
     * Validate that the given DOM Document conforms to the XSQL.xsd schema.
     *
     * @param doc
     *         The DOM Document to be validated.
     * @return <tt>true</tt> if the document is valid, otherwise an exception is thrown.
     * @throws DAOInitializationException
     */
    static boolean validateDocument(Document doc)
            throws DAOInitializationException {
        // Declared package private to allow unit testing.

        // Creating validators only when needed.
        if (docValidators == null) {
            docValidators = new ThreadLocalValidator("/org/cujau/db2/XSQL.xsd");
        }

        if (doc == null) {
            throw new DAOInitializationException("Can't validate a null DAO-XML Document.");
        }
        try {
            // Get the thread local Validator instance.
            Validator validator = docValidators.get();
            // Validate the document.
            validator.validate(new DOMSource(doc));
        } catch (SAXException e) {
            // The document is invalid.
            throw new DAOInitializationException("The DAO-XML document is invalid.", e);
        } catch (IOException e) {
            throw new DAOInitializationException("IOException validating the DAO-XML document.", e);
        }
        return true;
    }

}
