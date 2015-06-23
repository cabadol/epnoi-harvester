package es.upm.oeg.epnoi.harvester.domain;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

/**
 * Created by cbadenes on 22/06/15.
 */
public class CorpusList {


    public static void main(String[] args) throws FileNotFoundException {

        File directory = new File("/Users/cbadenes/Projects/epnoi-ressist/src/test/resources/json");
        Gson gson = new Gson();


        for (File json: directory.listFiles()){

            ResearchObject ro = gson.fromJson(new FileReader(json), ResearchObject.class);

            StringBuilder description = new StringBuilder();
            //\textit{source} 			& $0\%$ 		& $93.19\%$ 	& $0\%$ 		& $1.83\%$ 	& $0\%$ 		& $4.96\%$ 	& $0\%$  \\ \hline

            description.append("\\textit{").append(ro.getUri()).append("}").
                    append("\t& ").append(ro.getMetainformation().getTitle().replace("&","and").replace("\n"," ")).
                    append("\t& ").append(ro.getMetainformation().getDescription().replace("&","and").replace("\n"," "));


            StringBuilder authors = new StringBuilder();
            authors.append("\\begin{itemize}");
            for(Creator creator: ro.getMetainformation().getCreators()){

                authors.append("\\item ").append(creator.getSurname().trim()).append(",").append(creator.getName());

            }
            authors.append("\\end{itemize}");
            description.append("\t& ").append(authors).append("\\\\ \\hline");

            System.out.println(description.toString());


        }




    }

}
