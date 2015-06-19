package es.upm.oeg.epnoi.harvester;

import es.upm.oeg.epnoi.harvester.processor.ROBuilder;
import es.upm.oeg.epnoi.harvester.processor.UIAContextGenerator;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Created by cbadenes on 18/06/15.
 */
public abstract class BaseRouteBuilder extends RouteBuilder{

    @Autowired
    protected ROBuilder roBuilder;

    @Value("${input.path}")
    protected String inputDir;

    @Value("${output.path}")
    protected String outputDir;


}
