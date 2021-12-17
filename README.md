Admittedly, XML/SOAP and Web-Services might be old school techniques -- but they are still relevant ...

... thus, easy to operate [JAXB](https://jcp.org/en/jsr/detail?id=222) and [JAX-WS](https://jcp.org/en/jsr/detail?id=224) code is also still a reasonable objective to pursue:

# Informaticum's Plugins for customised JAXB/JAX-WS Java Code

This project offers some plugins to be used in conjunction with code generation by, e.g., the [Apache CXF WSDL-to-Java Plugin](https://cxf.apache.org/docs/maven-cxf-codegen-plugin-wsdl-to-java.html).

## Code-Reuse Plugin

Some of the generated code is kept `private` even though it is immutable.
For example, there are several [QName](https://docs.oracle.com/en/java/javase/17/docs/api/java.xml/javax/xml/namespace/QName.html) instances you might be interested in.

This plugin manipulates the access modifier of such code.
Just put the plugin into the [`wsdl2java`](https://cxf.apache.org/docs/wsdl-to-java.html)'s classpath and activate by providing the `xjc` argument `-informaticum-xjc-reuse`.
The plugin parameter(s) are:

* `-reuse-qnames`: Modify QName constants' accessibility to [public]. Default: false

In case you are using Maven and the CXF plugin, the configuration is:

```xml
<plugin>
  <groupId>org.apache.cxf</groupId>
  <artifactId>cxf-codegen-plugin</artifactId>
  <version>${current-cxf-version}</version>
  <dependencies>
    <dependency>
      <groupId>de.informaticum.xjc</groupId>
      <artifactId>xjc-plugins</artifactId>
      <version>${current-project-version}</version>
    </dependency>
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>${current-logback-version}</version>
    </dependency>
    [...]
  </dependencies>
  <executions>
    <execution>
      <id>generate-gematik-api-webservices</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>wsdl2java</goal>
      </goals>
      [...]
      <configuration>
        <defaultOptions>
          <xjcargs>
            <xjcarg>-informaticum-xjc-reuse</xjcarg>
            <xjcarg>-reuse-qnames</xjcarg>
          </xjcargs>
        </defaultOptions>
        [...]
      </configuration>
    </execution>
  </executions>
</plugin>
```

In result, you will see Code similar to:

```java
package org.w3._2006._05.addressing.wsdl;

import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {

    public final static QName _ServiceName_QNAME = new QName("http://www.w3.org/2006/05/addressing/wsdl", "ServiceName");
    public final static QName _InterfaceName_QNAME = new QName("http://www.w3.org/2006/05/addressing/wsdl", "InterfaceName");

    [...]
```

## Trace Plugin

This plugin traces all generated sources, mainly intended to enable debugging purposes.
(For this, the plugin -- likewise all other plugins -- uses [SLF4J](http://www.slf4j.org/) as the logging API.)
Just put the plugin into the [`wsdl2java`](https://cxf.apache.org/docs/wsdl-to-java.html)'s classpath and activate by providing the `xjc` argument `-informaticum-xjc-trace`.
The plugin parameter(s) are:

* none so far

In case you are using Maven and the CXF plugin, the configuration (exemplarily in combination with [logback](http://logback.qos.ch/) and a [custom logback configuration](http://logback.qos.ch/manual/configuration.html))
is:

```xml
<plugin>
  <groupId>org.apache.cxf</groupId>
  <artifactId>cxf-codegen-plugin</artifactId>
  <version>${current-cxf-version}</version>
  <dependencies>
    <dependency>
      <groupId>de.informaticum.xjc</groupId>
      <artifactId>xjc-plugins</artifactId>
      <version>${current-project-version}</version>
    </dependency>
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
      <version>${current-logback-version}</version>
    </dependency>
    [...]
  </dependencies>
  <executions>
    <execution>
      <id>generate-gematik-api-webservices</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>wsdl2java</goal>
      </goals>
      [...]
      <configuration>
        <additionalJvmArgs>-Dlogback.configurationFile=${project.basedir}/src/main/cxf/logback-for-cxf-plugin.xml</additionalJvmArgs>
        <defaultOptions>
          <xjcargs>
            <xjcarg>-informaticum-xjc-trace</xjcarg>
          </xjcargs>
        </defaultOptions>
        [...]
      </configuration>
    </execution>
  </executions>
</plugin>
```

In result, the logging messages will be printed wherever said by the logback configuration.
