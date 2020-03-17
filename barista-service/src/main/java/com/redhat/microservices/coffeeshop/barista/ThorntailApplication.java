package com.redhat.microservices.coffeeshop.barista;

import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@OpenAPIDefinition(
        info = @Info(
            title = "Barista",
            contact = @Contact(name = "Raphael Abreu", email = "raphael.alex@gmail.com"),
            version = "1.0.0",
            license = @License(
                name = "Apache 2.0",
                url = "http://www.apache.org/licenses/LICENSE-2.0.html")
        ),
        tags = {
                @Tag(name = "Coffee Shop Application")
        },
        externalDocs = @ExternalDocumentation(url = "www.github.com/aelkz/coffeeshop",
                description = "Coffee Shop External Documents")
)
@ApplicationPath("/api")
public class ThorntailApplication extends Application {
}
