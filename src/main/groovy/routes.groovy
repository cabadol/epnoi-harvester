import es.upm.oeg.epnoi.harvester.BaseRouteBuilder
import org.apache.camel.LoggingLevel
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class routes extends BaseRouteBuilder {

    protected static final Logger LOG = LoggerFactory.getLogger(routes.class);

    @Override
    public void configure() throws Exception {

        /*********************************************************************************************************************************
         * ROUTE 1: RSS
         *********************************************************************************************************************************/
        from("file:"+inputDir+"/rss?recursive=true&include=.*\\.xml&doneFileName=\${file:name}.done").
                to("direct:setCommonRssXpathExpressions").
                to("seda:notifyUIA")


        /*********************************************************************************************************************************
         * ROUTE 2: OAIPMH
         *********************************************************************************************************************************/
        from("file:"+inputDir+"/oaipmh?recursive=true&include=.*\\.xml&doneFileName=\${file:name}.done").
                to("direct:setCommonOaipmhXpathExpressions").
                to("seda:notifyUIA")


        /*********************************************************************************************************************************
         * -> To UIA
         *********************************************************************************************************************************/
        from("seda:notifyUIA").
                process(roBuilder).
                log(LoggingLevel.INFO,LOG,"File Read: '\${header.CamelFileName}'").
                to("file:"+outputDir+"?fileName=\${header.FileName}")

    }

}
