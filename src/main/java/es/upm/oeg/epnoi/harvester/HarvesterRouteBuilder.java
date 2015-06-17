package es.upm.oeg.epnoi.harvester;

import com.google.common.base.Joiner;
import es.upm.oeg.epnoi.harvester.processor.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.net.MalformedURLException;

public abstract class HarvesterRouteBuilder extends RouteBuilder {

    protected static final Logger LOG = LoggerFactory.getLogger(HarvesterRouteBuilder.class);

    private static Joiner joiner                                = Joiner.on(".");
    private static final String EPNOI                           = "epnoi";

    // Time
    public static final String TIME                             = joiner.join(EPNOI,"time");

    // File
    private static final String ARGUMENT                        = joiner.join(EPNOI,"argument");
    public static final String ARGUMENT_NAME                    = joiner.join(ARGUMENT,"name");
    public static final String ARGUMENT_PATH                    = joiner.join(ARGUMENT,"path");

    // Source
    private static final String SOURCE                          = joiner.join(EPNOI,"source");
    public static final String SOURCE_NAME                      = joiner.join(SOURCE,"name");
    public static final String SOURCE_URI                       = joiner.join(SOURCE, "uri");
    public static final String SOURCE_URL                       = joiner.join(SOURCE, "url");
    public static final String SOURCE_PROTOCOL                  = joiner.join(SOURCE,"protocol");

    // Publication
    private static final String PUBLICATION                     = joiner.join(EPNOI,"publication");
    public static final String PUBLICATION_UUID                 = joiner.join(PUBLICATION,"uuid");
    public static final String PUBLICATION_TITLE                = joiner.join(PUBLICATION,"title");
    public static final String PUBLICATION_DESCRIPTION          = joiner.join(PUBLICATION,"description");
    public static final String PUBLICATION_PUBLISHED            = joiner.join(PUBLICATION,"published");
    public static final String PUBLICATION_PUBLISHED_DATE       = joiner.join(PUBLICATION_PUBLISHED,"date");
    public static final String PUBLICATION_PUBLISHED_MILLIS     = joiner.join(PUBLICATION_PUBLISHED,"millis");
    public static final String PUBLICATION_URI                  = joiner.join(PUBLICATION,"uri");
    public static final String PUBLICATION_LANGUAGE             = joiner.join(PUBLICATION,"lang");
    public static final String PUBLICATION_RIGHTS               = joiner.join(PUBLICATION,"rights");
    public static final String PUBLICATION_FORMAT               = joiner.join(PUBLICATION,"format");
    //  -> urls
    public static final String PUBLICATION_URL                 = joiner.join(PUBLICATION,"url");
    public static final String PUBLICATION_URL_LOCAL            = joiner.join(PUBLICATION_URL,"local");

    //  -> reference
    private static final String PUBLICATION_REFERENCE           = joiner.join(PUBLICATION,"reference");
    public static final String PUBLICATION_METADATA_FORMAT = joiner.join(PUBLICATION_REFERENCE,"format");
    public static final String PUBLICATION_REFERENCE_URL        = joiner.join(PUBLICATION_REFERENCE,"url");
    //  -> creators
    public static final String PUBLICATION_CREATORS             = joiner.join(PUBLICATION,"creators"); //CSV


    @Autowired
    protected ErrorHandler errorHandler;

    @Autowired
    protected UIAContextGenerator contextGenerator;

    @Value("${storage.path}")
    protected String basedir;

    @Value("${uia.service.host}")
    protected String uiaServers;

    protected Namespaces ns = new Namespaces("oai", "http://www.openarchives.org/OAI/2.0/")
        .add("dc", "http://purl.org/dc/elements/1.1/")
        .add("provenance", "http://www.openarchives.org/OAI/2.0/provenance")
        .add("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/")
        .add("rss", "http://purl.org/rss/1.0/");

    @Override
    public void configure() throws Exception {

        onException(MalformedURLException.class)
                .process(errorHandler).stop();

        onException(IOException.class)
                .maximumRedeliveries(3)
                .process(errorHandler).stop();

        /*********************************************************************************************************************************
         * -> Set Common Rss Xpath Expressions
         *********************************************************************************************************************************/
        from("direct:setCommonRssXpathExpressions").
                setProperty(SOURCE_PROTOCOL, constant("rss")).
                setProperty(SOURCE_URI,                 simple("http://www.epnoi.org/rss/${property." + SOURCE_NAME + "}")).
                setProperty(SOURCE_NAME,                xpath("//rss:channel/rss:title/text()", String.class).namespaces(ns)).
                setProperty(SOURCE_URL,                 xpath("//rss:channel/rss:link/text()", String.class).namespaces(ns)).
                setProperty(PUBLICATION_TITLE,          xpath("//rss:item/rss:title/text()", String.class).namespaces(ns)).
                setProperty(PUBLICATION_DESCRIPTION,    xpath("//rss:item/rss:description/text()", String.class).namespaces(ns)).
                setProperty(PUBLICATION_PUBLISHED,      xpath("//rss:item/dc:date/text()", String.class).namespaces(ns)).
                setProperty(PUBLICATION_URI,            xpath("//rss:item/rss:link/text()", String.class).namespaces(ns)).
                setProperty(PUBLICATION_URL,            xpath("//rss:item/rss:link/text()", String.class).namespaces(ns)).
                setProperty(PUBLICATION_URL_LOCAL,      simple("${header.CamelFileAbsolutePath}")).
                setProperty(PUBLICATION_LANGUAGE,       xpath("//rss:channel/dc:language/text()", String.class).namespaces(ns)).
                setProperty(PUBLICATION_RIGHTS,         xpath("//rss:channel/dc:rights/text()", String.class).namespaces(ns)).
                setProperty(PUBLICATION_CREATORS,       xpath("string-join(//rss:channel/dc:creator/text(),\";\")", String.class).namespaces(ns)).
                setProperty(PUBLICATION_FORMAT,         constant("htm")).
                setProperty(PUBLICATION_METADATA_FORMAT,constant("xml")).
                setProperty(PUBLICATION_REFERENCE_URL,  simple("${header.CamelFileParent}/.camel/${header.CamelFileNameOnly}"));

        /*********************************************************************************************************************************
         * -> Set Common OAI-PMH Xpath Expressions
         *********************************************************************************************************************************/
        from("direct:setCommonOaipmhXpathExpressions").
                setProperty(SOURCE_PROTOCOL,            constant("oaipmh")).
                setProperty(SOURCE_URI,                 simple("http://www.epnoi.org/oaipmh/${property." + SOURCE_NAME + "}")).
                setProperty(SOURCE_NAME,                xpath("substring-before(substring-after(//oai:request/text(),\"http://\"),\"/\")", String.class).namespaces(ns)).
                setProperty(SOURCE_URL,                 xpath("//oai:request/text()", String.class).namespaces(ns)).
                setProperty(PUBLICATION_TITLE,          xpath("//oai:metadata/oai:dc/dc:title/text()", String.class).namespaces(ns)).
                setProperty(PUBLICATION_DESCRIPTION,    xpath("//oai:metadata/oai:dc/dc:description/text()",String.class).namespaces(ns)).
                setProperty(PUBLICATION_PUBLISHED,      xpath("//oai:header/oai:datestamp/text()",String.class).namespaces(ns)).
                setProperty(PUBLICATION_URI,            xpath("//oai:header/oai:identifier/text()",String.class).namespaces(ns)).
                setProperty(PUBLICATION_URL,            xpath("//oai:metadata/oai:dc/dc:identifier/text()",String.class).namespaces(ns)).
                setProperty(PUBLICATION_URL_LOCAL,      simple("${header.CamelFileAbsolutePath}")).
                setProperty(PUBLICATION_LANGUAGE,       xpath("//oai:metadata/oai:dc/dc:language/text()",String.class).namespaces(ns)).
                setProperty(PUBLICATION_RIGHTS,         xpath("//oai:metadata/oai:dc/dc:rights/text()", String.class).namespaces(ns)).
                setProperty(PUBLICATION_CREATORS,       xpath("string-join(//oai:metadata/oai:dc/dc:creator/text(),\";\")", String.class).namespaces(ns)).
                setProperty(PUBLICATION_FORMAT,         xpath("substring-after(//oai:metadata/oai:dc/dc:format/text(),\"/\")", String.class).namespaces(ns)).
                setProperty(PUBLICATION_METADATA_FORMAT,constant("xml")).
                setProperty(PUBLICATION_METADATA_FORMAT,constant("xml")).
                setProperty(PUBLICATION_REFERENCE_URL,  simple("${header.CamelFileParent}/.camel/${header.CamelFileNameOnly}"));




    }


}
