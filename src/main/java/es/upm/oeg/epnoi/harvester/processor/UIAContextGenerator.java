package es.upm.oeg.epnoi.harvester.processor;


import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import es.upm.oeg.camel.euia.model.Context;
import es.upm.oeg.camel.euia.model.Publication;
import es.upm.oeg.camel.euia.model.Reference;
import es.upm.oeg.camel.euia.model.Source;
import es.upm.oeg.epnoi.harvester.HarvesterRouteBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UIAContextGenerator implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(UIAContextGenerator.class);

    @Override
    public void process(Exchange exchange) throws Exception {

        // UIA Context
        Context context = new Context();

        // Source of data
        Source source = new Source();
        source.setName(exchange.getProperty(HarvesterRouteBuilder.SOURCE_NAME,String.class));
        source.setUri(exchange.getProperty(HarvesterRouteBuilder.SOURCE_URI,String.class));
        source.setUrl(exchange.getProperty(HarvesterRouteBuilder.SOURCE_URL,String.class));
        source.setProtocol(exchange.getProperty(HarvesterRouteBuilder.SOURCE_PROTOCOL,String.class));
        context.setSource(source);

        // Publication
        Publication publication = new Publication();

        // Metadata
        Reference reference = new Reference();
        reference.setFormat(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_METADATA_FORMAT,String.class));
        reference.setUrl("file://"+exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_REFERENCE_URL,String.class));
        publication.setReference(reference);

        publication.setTitle(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_TITLE,String.class));
        publication.setUri(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_URI, String.class));
        publication.setFormat(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_FORMAT, String.class));
        publication.setLanguage(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_LANGUAGE,String.class));
        publication.setPublished(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_PUBLISHED,String.class));
        publication.setRights(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_RIGHTS,String.class));
        publication.setUrl("file://" + exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_URL_LOCAL,String.class).replace("."+reference.getFormat(), "."+publication.getFormat()));


        publication.setDescription(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_DESCRIPTION,String.class));
        context.add(publication);

        Iterable<String> iterator = Splitter.on(';').trimResults().omitEmptyStrings().split(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_CREATORS, String.class));
        ArrayList<String> creators = Lists.newArrayList(iterator);
        publication.setCreators(creators);


        Gson gson = new Gson();
        String json = gson.toJson(context);


        exchange.getIn().setBody(json,String.class);

        LOG.debug("Json: {}", json);

    }
}

