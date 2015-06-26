package es.upm.oeg.epnoi.harvester.domain;

import com.google.common.base.CharMatcher;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

/**
 * Created by cbadenes on 22/06/15.
 */
public class CorpusList {


    public static void main(String[] args) throws FileNotFoundException {

        File directory = new File("/Users/cbadenes/Temp/epnoi-harvester-1.0.5/researchObjects/all");
        Gson gson = new Gson();


        for (File json: directory.listFiles()){

            ResearchObject ro = gson.fromJson(new FileReader(json), ResearchObject.class);

            StringBuilder description = new StringBuilder();
            //\textit{source} 			& $0\%$ 		& $93.19\%$ 	& $0\%$ 		& $1.83\%$ 	& $0\%$ 		& $4.96\%$ 	& $0\%$  \\ \hline

            description.
                    append("").append(StringUtils.substringBefore(StringUtils.substringAfterLast(ro.getSource().getUrl(), "index.php/"), "/").toUpperCase()).
                    append("\t&\\textit{").append(ro.getUri()).append("}").
                    append("\t& ").append(ro.getMetainformation().getTitle().replace("&", "and").replace("\n", " ").replace("%", "\\%"));
//                    .append("\t& ").append(CharMatcher.ASCII.retainFrom(ro.getMetainformation().getDescription()
//                        .replace("&", "and").replace("\n", " ").replace("\r", " ")
//                        .replace("%", "\\%").replace("{", "\\{").replace("}","\\}")));


            StringBuilder authors = new StringBuilder();
            authors.append("\\pbox{3cm}{");
            for(Creator creator: ro.getMetainformation().getCreators()){



                authors
//                        .append("\\pbox{20cm}{")
                        .append("* ")
                        .append(StringUtils.stripStart(creator.getSurname().trim()
                                .replace("&", "and").replace("\n", " ").replace("\r", " ")
                                .replace("%", "\\%").replace("{", "\\{").replace("}", "\\}")," "))
                        .append(",")
                        .append(StringUtils.stripStart(creator.getName()
                                .replace("&", "and").replace("\n", " ").replace("\r", " ")
                                .replace("%", "\\%").replace("{", "\\{").replace("}", "\\}")," "))
                                .append(" \\\\ ");
            }
            authors.append("}");
            description.append("\t& ").append(authors).append("\\\\ \\hline");

            System.out.println(description.toString());


        }




    }

}
