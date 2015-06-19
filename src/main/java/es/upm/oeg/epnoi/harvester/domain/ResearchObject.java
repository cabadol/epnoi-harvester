package es.upm.oeg.epnoi.harvester.domain;

import lombok.Data;

import java.lang.String;import java.util.List;

/**
 * Created by cbadenes on 19/06/15.
 */
@Data
public class ResearchObject {

    String uri;
    String url;
    ResearchSource source;
    MetaInformation metainformation;
    List<String> bagOfWords;
    List<ResearchObject> resources;

}
