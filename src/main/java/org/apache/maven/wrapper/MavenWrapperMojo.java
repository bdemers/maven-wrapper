/*
 * Copyright 2013. Yves Zoundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.maven.wrapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;

@Mojo(name = "wrapper", requiresProject = true)
/**
 * Quick and dirty implementation of a Maven goal for the maven-wrapper project
 * <a href="https://github.com/bdemers/maven-wrapper">https://github.com/bdemers/maven-wrapper</a>.
 * 
 * Test with Maven 3.0.x
 * 
 * @author Yves Zoundi
 */
public class MavenWrapperMojo extends AbstractMojo implements Contextualizable {

        static final String DIST_URL_TEMPLATE = "https://repository.apache.org/content/repositories/releases/org/apache/maven/apache-maven/%s/apache-maven-%s-bin.zip";
        static final String WRAPPER_PROPERTIES_FILE_NAME = "maven-wrapper.properties";
        static final String WRAPPER_ROOT_FOLDER_NAME = "maven";
        static final String WRAPPER_BASE_FOLDER_NAME = "wrapper";
        static final String WRAPPER_JAR_FILE_NAME = "maven-wrapper.jar";
        static final String DISTRIBUTION_URL_PROPERTY = "distributionUrl";

        static final String LAUNCHER_WINDOWS_FILE_NAME = "mvnw.bat";
        static final String LAUNCHER_UNIX_FILE_NAME = "mvnw";

        private PlexusContainer container;

        @Component
        private MavenProject project;

        @Component
        private PluginDescriptor plugin;

        public void contextualize(Context context) throws ContextException {
                container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
        }

        public void execute() throws MojoExecutionException {
                try {
                        Artifact mainArtifact = (Artifact) plugin.getPluginArtifact();

                        // Get the maven version information via Plexus
                        RuntimeInformation runtimeInformation = container.lookup(RuntimeInformation.class);
                        final String mavenVersion = runtimeInformation.getMavenVersion();

                        File baseDir = project.getBasedir();

                        final String[] launcherFileNames = { LAUNCHER_WINDOWS_FILE_NAME, LAUNCHER_UNIX_FILE_NAME };
                        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                        
                        for (String launcherFileName : launcherFileNames) {
                                InputStream mvnLauncherStream = classLoader.getResourceAsStream(launcherFileName);
                                File launcherFile = new File(baseDir, launcherFileName);
                                writeToFile(mvnLauncherStream, launcherFile);

                                if (!launcherFile.setExecutable(true)) {
                                        getLog().warn("Could not set executable flag on file: " + launcherFile.getAbsolutePath());
                                }
                        }

                        File baseMavenFolder = new File(baseDir, WRAPPER_ROOT_FOLDER_NAME);
                        File wrapperDestFolder = new File(baseMavenFolder, WRAPPER_BASE_FOLDER_NAME);
                        wrapperDestFolder.mkdirs();

                        Properties props = new Properties();
                        props.put(DISTRIBUTION_URL_PROPERTY, String.format(DIST_URL_TEMPLATE, mavenVersion, mavenVersion));
                        File file = new File(wrapperDestFolder, WRAPPER_PROPERTIES_FILE_NAME);
                        FileOutputStream fileOut = null;
                        InputStream is = null;
                        try {
                                is = new FileInputStream(mainArtifact.getFile());
                                writeToFile(is, new File(wrapperDestFolder, WRAPPER_JAR_FILE_NAME));
                                fileOut = new FileOutputStream(file);
                                props.store(fileOut, "Maven download properties");

                        }
                        finally {
                                if (fileOut != null) {
                                        fileOut.close();
                                }
                                if (is != null) {
                                        is.close();
                                }
                        }
                }
                catch (Exception e) {
                        throw new RuntimeException("Could not detect Maven version", e);
                }
        }

        private static void writeToFile(InputStream stream, File filePath) throws IOException {
                FileChannel outChannel = null;
                ReadableByteChannel inChannel = null;
                FileOutputStream fos = null;

                try {
                        fos = new FileOutputStream(filePath);
                        outChannel = fos.getChannel();
                        inChannel = Channels.newChannel(stream);
                        ByteBuffer buffer = ByteBuffer.allocate(1024);

                        while (inChannel.read(buffer) >= 0 || buffer.position() > 0) {
                                buffer.flip();
                                outChannel.write(buffer);
                                buffer.clear();
                        }
                }
                finally {
                        if (inChannel != null) {
                                inChannel.close();
                        }

                        if (outChannel != null) {
                                outChannel.close();
                        }

                        if (fos != null) {
                                fos.close();
                        }

                        if (stream != null) {
                                stream.close();
                        }
                }
        }
}
