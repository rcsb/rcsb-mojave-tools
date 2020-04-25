package org.rcsb.mojave.tools.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * Created on 9/28/18.
 *
 * @author Yana Valasatava
 * @since
 */
public class TestCommonUtils {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Test
    public void shouldEnsurePathExists() throws IOException {

        String root = tempFolder.getRoot().getPath();
        String path = "one/two/three";

        File folder = new File(String.join("/", root, path));
        folder.deleteOnExit();
        CommonUtils.ensurePathToFolderExist(folder);

        assertTrue(folder.exists());
    }
}
