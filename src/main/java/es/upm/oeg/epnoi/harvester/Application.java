package es.upm.oeg.epnoi.harvester;

import groovy.lang.GroovyClassLoader;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;


@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {

    @Value("${camel.config.groovy}")
    File groovyFile;


    @Autowired
    RouteBuilder routeBuilder;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RouteBuilder groovyRouteBuilder() throws IOException, IllegalAccessException, InstantiationException {
        // Loading groovy class
        GroovyClassLoader gcl = new GroovyClassLoader();
        Class clazz = gcl.parseClass(groovyFile);
        return (AbstractRouteBuilder) clazz.newInstance();
    }


    @Bean
    public SpringCamelContext camelContext(ApplicationContext applicationContext) throws Exception {

        Main main = new Main();

        SpringCamelContext camelContext = new SpringCamelContext(applicationContext);
        camelContext.addRoutes(routeBuilder);
        return camelContext;
    }



}
