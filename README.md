[![Maven Central](https://img.shields.io/maven-central/v/io.bootique.tools/hugo-asciidoctorj-extension.svg?colorB=brightgreen)](https://search.maven.org/artifact/io.bootique.tools/hugo-asciidoctorj-extension)

# AsciidoctorJ Hugo Extension 

AsciidoctorJ post processor, that extracts ToC into separate file and optionally can inject content into rendered document. 
Can be used only for HTML backend, will **fail** if used with PDF.

It is targeted to inject `front-matter` section suitable for the [Hugo](https://gohugo.io) website generator.

## Usage

Here's a simple usage example:  

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.asciidoctor</groupId>
            <artifactId>asciidoctor-maven-plugin</artifactId>
            <version>${asciidoctor.maven.plugin.version}</version>
            <dependencies>
                <dependency>
                    <groupId>io.bootique.tools</groupId>
                    <artifactId>hugo-asciidoctorj-extension</artifactId>
                    <version>${hugo.asciidoctorj.extension.version}</version>
                </dependency>
            </dependencies>
            <executions>
                <execution>
                    <id>asciidoctor-html-hugo</id>
                    <phase>generate-resources</phase>
                    <goals>
                        <goal>process-asciidoc</goal>
                    </goals>
                    <configuration>
                        <doctype>book</doctype>
                        <backend>html5</backend>
                        <!-- ... -->
                        <extensions>
                            <extension>
                                <className>io.bootique.tools.asciidoctorj.HugoExtension</className>
                            </extension>
                        </extensions>
                        <attributes>
                            <!-- ... -->
                        </attributes>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
    <!-- ...   -->
</build>
```

Projects using this extension:

- [Agrest documentation](https://github.com/agrestio/agrest-docs)
- [Bootique documentation](https://github.com/bootique/bootique/tree/master/bootique-docs)
- [Apache Cayenne documentation](https://github.com/apache/cayenne/tree/master/docs/asciidoc)

## Control Attributes

Extension controlled by attributes in `*.adoc` file:
- `hugo-header`: header file name, if not set empty 'front-matter' will be used
- `hugo-font-awesome-icons`: convert icons to a FontAwesome compatible variant (`true`/`false`, default is `true`)
- `hugo-multipage`: split document to a separate pages (`true`/`false`, default is `false`)
- `hugo-multipage-level`: nested section level that will be used for a single page content in a multipage documents (default is `1`)
- `hugo-multipage-header`: header file name to add to a multipage documents, index page will always use `hugo-header`, if not set empty 'front-matter' will be used
- `hugo-multipage-ref`: reference prefix for a multipage navigation (could be something like `/docs/1.x/`)

## Support

You could open an issue or a feature request via [GitHub Issues](https://github.com/bootique-tools/hugo-asciidoctorj-extension/issues) 

## Development

You need Java 8+ and Maven.

### Release

```bash
mvn release:prepare -Prelease
mvn release:perform -Prelease
```