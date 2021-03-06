/*
 Copyright (c) 2008 Charles A. Loomis, Jr, Cedric Duprilot, and
 Centre National de la Recherche Scientifique (CNRS).

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 $HeadURL: https://svn.lal.in2p3.fr/LCG/QWG/panc/trunk/src/org/quattor/pan/utils/TermTest.java $
 $Id: TermTest.java 3550 2008-08-02 14:54:26Z loomis $
 */

package org.quattor.pan.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.quattor.pan.utils.TestUtils.getTmpdir;
import static org.quattor.pan.utils.TestUtils.recursiveFileDelete;
import static org.quattor.pan.utils.TestUtils.touch;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileSystemSourceRepositoryTest {

    private final static File tmpdir = new File(getTmpdir(),
            FileSystemSourceRepositoryTest.class.getSimpleName());

    enum SourceDirectory {
        INCLUDE1, INCLUDE2;

        final public int mask;

        private SourceDirectory() {
            mask = (0x1 << ordinal());
        }

        public static int getNumberOfValues() {
            return SourceDirectory.values().length;
        }

        public static int getLimit() {
            return (int) (Math.pow(2, SourceDirectory.getNumberOfValues()) - 1);
        }

        public boolean isPresent(int bits) {
            return (mask & bits) != 0;
        }

        public File asDirectory(File root) {
            return new File(root, toString());
        }

        public String asDirectoryString(File root) {
            return asDirectory(root).toString();
        }

        public static SourceDirectory valueFromMask(int mask) {

            for (SourceDirectory sd : SourceDirectory.values()) {
                if ((sd.mask & mask) != 0) {
                    return sd;
                }
            }
            return null;
        }

    }

    public FileSystemSourceRepositoryTest() {

    }

    @BeforeClass
    public static void createTestFiles() throws IOException {

        removeAndRecreateTmpdir();

        setupTestFiles();

    }

    @AfterClass
    public static void deleteTestFiles() throws IOException {
        // recursiveFileDelete(tmpdir);
    }

    @Test
    public void nullParametersOK() {
        SourceRepository repository;
        repository = FileSystemSourceRepository.getInstance(null);

        notNullAndCorrectType(repository);
    }

    @Test
    public void emptyParametersOK() {

        ParameterList parameters = new ParameterList();

        SourceRepository repository;
        repository = FileSystemSourceRepository.getInstance(parameters);

        notNullAndCorrectType(repository);
    }

    @Test
    public void checkCorrectClass() {
        ParameterList parameters = getParameters();

        SourceRepository repository;
        repository = FileSystemSourceRepository.getInstance(parameters);

        assertTrue(repository instanceof FileSystemSourceRepositoryImpl);
    }

    @Test
    public void checkNormalPanLookupOrder() throws IOException {

        ParameterList parameters = getParameters();
        SourceRepository repository;
        repository = FileSystemSourceRepository.getInstance(parameters);

        for (int i = 0; i <= SourceDirectory.getLimit(); i++) {

            String name = Integer.toBinaryString(i);

            SourceFile source = repository.retrievePanSource(name);
            SourceDirectory sd = SourceDirectory.valueFromMask(i);

            if (!source.isAbsent()) {
                File location = source.getLocation();
                assertEquals(location, sd.asDirectory(tmpdir));
            } else {
                assertNull(sd);
            }

        }

    }

    @Test
    public void checkNormalTxtLookupOrder() throws IOException {

        ParameterList parameters = getParameters();
        SourceRepository repository;
        repository = FileSystemSourceRepository.getInstance(parameters);

        for (int i = 0; i <= SourceDirectory.getLimit(); i++) {

            String name = Integer.toBinaryString(i) + ".pan";

            SourceFile source = repository.retrieveTxtSource(name);
            SourceDirectory sd = SourceDirectory.valueFromMask(i);

            if (!source.isAbsent()) {
                File location = source.getLocation();
                assertEquals(location, sd.asDirectory(tmpdir));
            } else {
                assertNull(sd);
            }

        }

    }

    public static void setupTestFiles() throws IOException {

        // Now create the include directories.
        for (SourceDirectory sd : SourceDirectory.values()) {
            createDirectory(sd.asDirectory(tmpdir));
        }

        // Create all combinations of existing and non-existing files in the
        // include directories.
        for (int i = 0; i <= SourceDirectory.getLimit(); i++) {

            for (SourceDirectory sd : SourceDirectory.values()) {

                if (sd.isPresent(i)) {
                    File directory = sd.asDirectory(tmpdir);
                    String fname = Integer.toBinaryString(i) + ".pan";

                    touch(directory, fname);
                }

            }

        }

        // Setup the tests for the *.del files.
        for (SourceDirectory sd1 : SourceDirectory.values()) {
            for (SourceDirectory sd2 : SourceDirectory.values()) {

                if (sd2.ordinal() >= sd1.ordinal()) {

                    File directory = sd2.asDirectory(tmpdir);

                    String tname = "D" + Integer.toBinaryString(sd1.ordinal())
                            + ".pan";
                    touch(directory, tname);

                    if (sd1.ordinal() == sd2.ordinal()) {
                        String dname = "D"
                                + Integer.toBinaryString(sd1.ordinal())
                                + ".del";
                        touch(directory, dname);
                        dname = "D" + Integer.toBinaryString(sd1.ordinal())
                                + ".pan.del";
                        touch(directory, dname);
                    }
                }

            }
        }

    }

    private void notNullAndCorrectType(SourceRepository repository) {
        assertNotNull(repository);
        assertTrue(repository instanceof FileSystemSourceRepository);
    }

    private ParameterList getParameters() {
        ParameterList parameters = new ParameterList();
        parameters.append("includeDirectory",
                SourceDirectory.INCLUDE1.asDirectoryString(tmpdir));
        parameters.append("includeDirectory",
                SourceDirectory.INCLUDE2.asDirectoryString(tmpdir));
        return parameters;
    }

    private static void removeAndRecreateTmpdir() {

        if (tmpdir.exists()) {
            recursiveFileDelete(tmpdir);
        }

        createDirectory(tmpdir);
    }

    private static void createDirectory(File directory) {
        if (!directory.mkdirs()) {
            throw new RuntimeException("could not create directory "
                    + directory);
        }
    }

}
