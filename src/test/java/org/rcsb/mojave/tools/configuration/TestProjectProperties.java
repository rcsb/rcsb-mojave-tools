package org.rcsb.mojave.tools.configuration;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * Tests ensure that project properties file is generated during build time and that this file
 * contains important properties used in the code base.
 *
 * Created on 8/31/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class TestProjectProperties {

    private static File resources;

    @BeforeClass
    public static void setup() {
        resources = new File("src/main/resources/tools.module.properties");
    }

    @Test
    public void shouldGenerateProjectPropertiesFile() {
        assertTrue(resources.exists());
    }

    @Test
    public void shouldIncludePropertyForAutoGeneratedUniProtSources() throws IOException {

        InputStream is = new FileInputStream(resources);
        Properties p = new Properties();
        p.load(is);

        assertNotNull(p.getProperty("uniprot.auto.package"));
    }
}
