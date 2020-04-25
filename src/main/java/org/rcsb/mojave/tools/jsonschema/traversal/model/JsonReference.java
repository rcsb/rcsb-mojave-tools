package org.rcsb.mojave.tools.jsonschema.traversal.model;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Representation of a JSON Reference. JSON Reference allows a JSON value
 * to reference another value in a JSON document.
 *
 * To quote the draft, "A JSON Reference is a JSON object, which contains
 * a member named "$ref", which has a JSON string value." This string value
 * must be a URI. Example:
 * <pre>
 *     {
 *         "$ref": "http://example.com/example.json#/foo/bar"
 *     }
 * </pre>
 *
 * <p>
 * The implementation is a wrapper over Java's {@link URI}, with the
 * following characteristics:
 * <ul>
 *     <li>an empty locator is equivalent to no locator at all;</li>
 *     <li>a reference is taken to be absolute.</li>
 * </ul>
 * </p>
 * Created on 1/2/20.
 *
 * @author Yana Valasatava
 * @since 1.0.0
 */
public class JsonReference {

    private URI uri;

    public JsonReference(URI uri) {
        config(uri);
    }

    public JsonReference(String ref) {
        config(asURI(ref));
    }

    private void config(URI uri) {
        this.uri = uri;
        if (getLocator() == null && getFragment() == null)
            throw new IllegalStateException("Failed to build JsonSchemaRef for "+uri.toString()
                    +"Locator, fragment or both MUST be defined.");
    }

    private URI asURI(String ref) {
        try {
            return new URI(ref);
        } catch (URISyntaxException e) {
            throw new RuntimeException("The string value of '$ref' does not conform to URI syntax rules. " +
                    "Error: " + e.getMessage());
        }
    }

    public URI getURI() {
        return uri;
    }

    public String getScheme() {
        return uri.getScheme();
    }

    public String getLocator() {
        return uri.getSchemeSpecificPart().isEmpty() ? null : uri.getSchemeSpecificPart();
    }

    public String getFragment() {
        return uri.getFragment();
    }

    public String getFullPath() {
        return uri.getPath();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder().append(getFullPath()).toHashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (!(obj instanceof JsonReference))
            return false;
        final JsonReference that = (JsonReference) obj;
        return this.uri.equals(that.uri);
    }
}
