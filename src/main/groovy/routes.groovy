import es.upm.oeg.epnoi.harvester.HarvesterRouteBuilder
import org.apache.camel.LoggingLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class routes extends HarvesterRouteBuilder{

    protected static final Logger LOG = LoggerFactory.getLogger(routes.class);

    @Override
    public void configure() throws Exception {
        super.configure()

        /*********************************************************************************************************************************
         * ROUTE 1: RSS
         *********************************************************************************************************************************/
        from("file:"+basedir+"/rss?recursive=true&include=.*\\.xml&doneFileName=\${file:name}.done").
                to("direct:setCommonRssXpathExpressions").
                to("seda:notifyUIA")


        /*********************************************************************************************************************************
         * ROUTE 2: OAIPMH
         *********************************************************************************************************************************/
        from("file:"+basedir+"/oaipmh?recursive=true&include=.*\\.xml&doneFileName=\${file:name}.done").
                to("direct:setCommonOaipmhXpathExpressions").
                to("seda:notifyUIA")


        /*********************************************************************************************************************************
         * -> To UIA
         *********************************************************************************************************************************/
        from("seda:notifyUIA").
                process(contextGenerator).
                log(LoggingLevel.INFO,LOG,"File Read: '\${header.CamelFileName}'").
                //to("euia:out?servers="+ uiaServers)
                to("stream:out")

    }

}
