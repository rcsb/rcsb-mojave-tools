package org.rcsb.mojave.tools.download;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.rcsb.mojave.tools.utils.ConfigurableMapper;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static java.util.Arrays.asList;

/**
 * Created on 4/30/20.
 *
 * @author Yana Valasatava
 * @since 1.2.0
 */
public class JarAssetDownloader implements AssetDownloader {

    private URL url;
    private List<String> assetNames;

    public static class Builder {

        private String url;
        private List<String> assetNames;

        public Builder fromUrl(String val) {
            this.url = val;
            return this;
        }

        public Builder forAssets(List<String> val) {
            this.assetNames = val;
            return this;
        }

        public JarAssetDownloader build() throws MalformedURLException {
            JarAssetDownloader instance = new JarAssetDownloader();
            instance.url = new URL(this.url);
            instance.assetNames = this.assetNames;
            return instance;
        }
    }

    private boolean includeName(String name) {
        if (assetNames == null || assetNames.isEmpty())
            return name.endsWith(".json");
        else return assetNames.contains(name);
    }

    /**
     * Read all the bytes for the current Jar entry.
     *
     * @param in delivers the contents of each Jar entry.
     * @return Jar entry content as bytes.
     * @throws IOException when cannot read from {@param in}.
     */
    private static byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(in, out);
        return out.toByteArray();
    }

    @Override
    public Map<String, JsonNode> requestSchemaAssets() throws IOException {

        InputStream is = url.openStream();
        JarInputStream jis = new JarInputStream(is);

        JarEntry entry;
        Map<String, JsonNode> assets = new HashMap<>();
        while ((entry = jis.getNextJarEntry()) != null) {
            String name = entry.getName();
            if (entry.isDirectory() || !includeName(name)) continue;
            byte[] arr = readBytes(jis);
            JsonNode schemaTree = ConfigurableMapper.getMapper().readTree(arr);
            assets.putIfAbsent(name, schemaTree);
        }
        return assets;
    }
}
