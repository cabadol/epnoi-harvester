package es.upm.oeg.epnoi.harvester.domain;

import lombok.Data;

import java.lang.String;import java.util.List;

/**
 * Created by cbadenes on 19/06/15.
 */
@Data
public class MetaInformation {

    String title;
    String published;
    String format;
    String language;
    String rights;
    String description;
    List<Creator> creators;
}
