package org.rcsb.mojave.tools.download;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Map;

/**
 * Created on 4/30/20.
 *
 * @author Yana Valasatava
 * @since 1.2.0
 */
public interface AssetDownloader {

    Map<String, JsonNode> requestSchemaAssets() throws IOException;
}
