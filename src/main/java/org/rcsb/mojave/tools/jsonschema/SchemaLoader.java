package org.rcsb.mojave.tools.jsonschema;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rcsb.mojave.tools.core.GenerateCombinedJsonSchema;
import org.rcsb.mojave.tools.utils.CommonUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * IO operations on a JSON representation of JSON schema.
 *
 * Created on 8/27/18.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class SchemaLoader {

    private final ObjectMapper objectMapper;

    private static final String JAR_SCHEME = "jar";
    private static final String FILE_SCHEME = "file";
    private static final String HTTP_SCHEME = "http";
    private static final String HTTPS_SCHEME = "https";

    public SchemaLoader() {
        this(null);
    }

    public SchemaLoader(JsonFactory jsonFactory) {
        this.objectMapper = (new ObjectMapper(jsonFactory))
                .enable(JsonParser.Feature.ALLOW_COMMENTS)
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    }

    public static boolean hasScheme(String path) {
        return path.startsWith(JAR_SCHEME+":")
                || path.startsWith(FILE_SCHEME+":")
                || path.startsWith(HTTP_SCHEME+":")
                || path.startsWith(HTTPS_SCHEME+":");
    }

    public void writeSchema(String filePath, JsonNode node) throws IOException {

        Object json = this.objectMapper.readValue(node.toString(), Object.class);
        String schemaString = this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        CommonUtils.ensurePathToFolderExist(new File(filePath).getParentFile());
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.write(schemaString);
        }
    }

    public JsonNode readSchema(InputStream is) throws IOException {
        return this.objectMapper.readTree(is);
    }

    public JsonNode readSchema(URL schemaUrl) throws IOException {
        return this.objectMapper.readTree(schemaUrl);
    }

    public JsonNode readSchema(File schemaFile) throws IOException {
        return this.objectMapper.readTree(schemaFile);
    }

    public JsonNode readSchema(String path) throws IOException {

        if (path.startsWith(JAR_SCHEME)) {
            path = path.replace(JAR_SCHEME+":", "");
            InputStream is = GenerateCombinedJsonSchema.class.getResourceAsStream(path);
                if (is == null)
                    throw new IOException("Cannot read schema from "+path+". Resource doesn't exist.");
                return readSchema(is);
        } else if (path.startsWith(FILE_SCHEME)) {
            URL url = new URL(path);
                if ( !Paths.get(url.getPath()).toFile().exists() )
                    throw new IOException("Cannot read schema from "+path+". File doesn't exist.");
                return readSchema(url);
        } else
            throw new IllegalArgumentException("Unsupported resource schema for: " + path);
    }

    public JsonNode readSchema(URI uri) throws IOException {

        if (uri.getScheme() == null ) {

            InputStream is = SchemaLoader.class.getResourceAsStream(uri.getSchemeSpecificPart());
            if (is == null)
                throw new IOException("Cannot read schema from " + uri.getSchemeSpecificPart() + ". Resource doesn't exist.");
            return readSchema(is);

        } else if (uri.getScheme().equals(JAR_SCHEME)) {
            InputStream is = SchemaLoader.class.getResourceAsStream(uri.getSchemeSpecificPart());
            if (is == null)
                throw new IOException("Cannot read schema from " + uri.getSchemeSpecificPart() + ". JAR resource doesn't exist.");
            return readSchema(is);

        } else if (uri.getScheme().equals(FILE_SCHEME)) {
            URL url = uri.toURL();
            if ( !Paths.get(url.getPath()).toFile().exists() )
                throw new IOException("Cannot read schema from "+uri.getSchemeSpecificPart()+". File doesn't exist.");
            return readSchema(url);

        } else if (uri.getScheme().equals(HTTP_SCHEME) || uri.getScheme().equals(HTTPS_SCHEME)) {
            throw new UnsupportedOperationException("Please, implement scheme: "+uri.getScheme());

        } else
            throw new UnsupportedOperationException("Loading URI with scheme: "+uri.getScheme()+" is not supported.");
    }
}
