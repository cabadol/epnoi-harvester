package es.upm.oeg.epnoi.harvester.processor;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import es.upm.oeg.epnoi.harvester.HarvesterRouteBuilder;
import es.upm.oeg.epnoi.harvester.domain.Creator;
import es.upm.oeg.epnoi.harvester.domain.MetaInformation;
import es.upm.oeg.epnoi.harvester.domain.ResearchObject;
import es.upm.oeg.epnoi.harvester.domain.ResearchSource;
import es.upm.oeg.epnoi.harvester.feature.LuceneClassifier;
import es.upm.oeg.epnoi.harvester.feature.PDFExtractor;
import es.upm.oeg.epnoi.harvester.feature.WordValidator;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cbadenes on 17/06/15.
 */
@Component
public class ROBuilder implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(ROBuilder.class);

    @Value("${input.path}")
    protected String inputDir;

    @Override
    public void process(Exchange exchange) throws Exception {

        ResearchObject researchObject = new ResearchObject();


        // URI
        researchObject.setUri(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_URI, String.class));

        // URL
        String refFormat    = exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_METADATA_FORMAT,String.class);
        String pubFormat    = exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_FORMAT, String.class);
        String path         = exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_URL_LOCAL,String.class).replace("."+refFormat, "."+pubFormat);
        researchObject.setUrl("file://" + path);

        // Source
        ResearchSource source = new ResearchSource();
        source.setName(exchange.getProperty(HarvesterRouteBuilder.SOURCE_NAME,String.class));
        source.setUrl(exchange.getProperty(HarvesterRouteBuilder.SOURCE_URL,String.class));
        source.setUri(exchange.getProperty(HarvesterRouteBuilder.SOURCE_URI,String.class)+StringUtils.substringAfter(source.getUrl(),"//"));

        source.setProtocol(exchange.getProperty(HarvesterRouteBuilder.SOURCE_PROTOCOL,String.class));
        researchObject.setSource(source);

        // Meta Information
        MetaInformation metaInformation = new MetaInformation();
        metaInformation.setTitle(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_TITLE, String.class));
        metaInformation.setPublished(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_PUBLISHED, String.class));
        metaInformation.setFormat(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_FORMAT, String.class));
        metaInformation.setLanguage(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_LANGUAGE, String.class));
        metaInformation.setRights(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_RIGHTS, String.class));
        metaInformation.setDescription(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_DESCRIPTION, String.class));


        // ->   Authors
        Iterable<String> iterator = Splitter.on(';').trimResults().omitEmptyStrings().split(exchange.getProperty(HarvesterRouteBuilder.PUBLICATION_CREATORS, String.class));
        ArrayList<String> authors = Lists.newArrayList(iterator);
        List<Creator> creators = new ArrayList<Creator>();

        for(String author: authors){

            String[] tokens = author.split(",");

            Creator creator = new Creator();
            creator.setName(tokens[1].trim());
            creator.setSurname(tokens[0].trim());
            String authorUri = "http://resources.ressist.es/author/" + creator.getSurname().trim() + "-" + creator.getName().trim();
            creator.setUri(UrlEscapers.urlFragmentEscaper().escape(authorUri));

            creators.add(creator);
        }

        metaInformation.setCreators(creators);

        // add metainformation to research object
        researchObject.setMetainformation(metaInformation);

        // BagOfWords: Lucene Stemming from PDF
        List<LuceneClassifier.Keyword> values = LuceneClassifier.guessFromString(PDFExtractor.from(path));

        List<String> words = new ArrayList<String>();

        for(LuceneClassifier.Keyword keyword: values){
            if (WordValidator.isValid(keyword.getStem())) words.add(keyword.getStem());
        }
        researchObject.setBagOfWords(words);


        // Convert to json
        Gson gson = new Gson();
        String json = gson.toJson(researchObject);


        // Put in camel flow
        exchange.getIn().setHeader("FileName", StringUtils.replace(StringUtils.substringAfterLast(researchObject.getUrl(), inputDir), metaInformation.getFormat(), "json"));
        exchange.getIn().setBody(json, String.class);


        LOG.info("ResearchObject as RegularResource: {}", researchObject);

    }
}
