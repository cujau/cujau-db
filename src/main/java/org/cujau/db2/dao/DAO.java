package org.cujau.db2.dao;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;

import org.cujau.db2.AbstractDBUtility;
import org.cujau.db2.DAOInitializationException;
import org.cujau.db2.jdbc.CujauJDBCTemplate;
import org.cujau.xml.ThreadLocalValidator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Base DAO class providing the standard DAO functionality. Extending classes must have defined an
 * XSQL file containing the specific SQL queries used by this DAO.
 */
public abstract class DAO {

    private static final ThreadLocalValidator VALIDATORS =
        new ThreadLocalValidator( "/org/cujau/db2/XSQL.xsd" );

    protected AbstractDBUtility dbutil;
    protected Map<String, String> queryMap;
    protected DataSource datasource;
    protected CujauJDBCTemplate template;
    protected Properties dialectProps;

    public DAO( Properties props, AbstractDBUtility dbutil )
            throws DAOInitializationException {
        dialectProps = props;
        queryMap = loadQueries( getClassForQueriesLoading(), dialectProps );
        this.dbutil = dbutil;
    }

    public AbstractDBUtility getDBUtility() {
        return dbutil;
    }
    
    public String getPrefix() {
        String prefix = dialectProps.getProperty( AbstractDBUtility.DB_PREFIX_PROPERTY );
        if ( prefix == null )
            prefix = "";
        return prefix;
    }

    protected Class<?> getClassForQueriesLoading() {
        return getClass();
    }

    /**
     * Extending classes can override to create specific JDBC templates (such as the
     * SimpleInsertTemplate).
     * 
     * @param dataSource
     */
    public void setDataSource( DataSource dataSource ) {
        datasource = dataSource;
        template = new CujauJDBCTemplate( dataSource );
    }

    public String getCreateQuery() {
        return queryMap.get( QueryLoader.CREATE_QUERY_NAME );
    }

    public String getDropQuery() {
        return queryMap.get( QueryLoader.DROP_QUERY_NAME );
    }

    public String getQuery( String id ) {
        return queryMap.get( id );
    }

    /**
     * Executes any SELECT statement on the connected database.
     */
    public ResultSet executeSelectQuery( String select )
            throws Exception {
        Statement statment = template.getDataSource().getConnection().createStatement();
        return statment.executeQuery( select );
    }

    protected String getQueryWithReplacement( String id, String placeholderPropName, String replacementStr ) {
        String baseStr = queryMap.get( id );
        if ( baseStr == null ) {
            return null;
        }
        return baseStr.replace( "${" + placeholderPropName + "}", replacementStr );
    }

    /**
     * Validate that the given DOM Document conforms to the XSQL.xsd schema.
     * 
     * @param doc
     *            The DOM Document to be validated.
     * @return <tt>true</tt> if the document is valid, otherwise an exception is thrown.
     * @throws DAOInitializationException
     */
    boolean validateDocument( Document doc )
            throws DAOInitializationException {
        // Declared package private to allow unit testing.
        if ( doc == null ) {
            throw new DAOInitializationException( "Can't validate a null XSQL Document." );
        }
        try {
            // Get the thread local Validator instance.
            Validator validator = VALIDATORS.get();
            // Validate the document.
            validator.validate( new DOMSource( doc ) );
        } catch ( SAXException e ) {
            // The document is invalid.
            throw new DAOInitializationException( "The XSQL document is invalid.", e );
        } catch ( IOException e ) {
            throw new DAOInitializationException( "IOException validating the XSQL document.", e );
        }
        return true;
    }

    protected Map<String, String> loadQueries( Class<?> klass, Properties props )
            throws DAOInitializationException {
        Map<String, String> queries = null;
        try {
            queries = QueryLoader.loadQueries( klass, props );
        } catch ( ParserConfigurationException e ) {
            throw new DAOInitializationException( "Exception loading queries for " + getClass().getName(), e );
        } catch ( SAXException e ) {
            throw new DAOInitializationException( "Exception loading queries for " + getClass().getName(), e );
        } catch ( IOException e ) {
            throw new DAOInitializationException( "Exception loading queries for " + getClass().getName(), e );
        }
        return queries;
    }
}
