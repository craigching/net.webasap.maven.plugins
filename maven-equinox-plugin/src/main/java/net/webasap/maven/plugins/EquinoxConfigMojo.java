package net.webasap.maven.plugins;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Goal which touches a timestamp file.
 *
 * @goal config
 * 
 * @phase process-sources
 * @requiresDependencyResolution compile
 */
public class EquinoxConfigMojo
    extends AbstractMojo
{
	final static private String LINE_SEP = System.getProperty("line.separator");
    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
    /**
     * @parameter
     */
    private String bundlesDir;

    /**
     * Location of the file.
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;
    
    /**
     * @parameter
     */
    private Properties properties;
    
    /**
     * @parameter
     */
    private List<BundleGroup> bundleGroups;
    
    public void execute()
        throws MojoExecutionException
    {
    	for (BundleGroup bg : this.bundleGroups) {
    		getLog().info("====== Bundle Group: Start Level: " + bg.getStartLevel());
    		String [] includes = bg.getIncludes();
    		for (String i : includes) {
    			getLog().info("Bundle: " + i);
    		}
    		getLog().info("=================");
    	}

        File f = outputDirectory;

        if ( !f.exists() )
        {
            f.mkdirs();
        }

        File touch = new File( f, "config.ini" );

        FileWriter w = null;
        try
        {
            w = new FileWriter( touch );

        	Enumeration<?> p = properties.propertyNames();
        	while(p.hasMoreElements()) {
        		String k = (String)p.nextElement();
        		String v = properties.getProperty(k);
        		w.write(k + "=" + v + LINE_SEP);
        	}
        	w.write("osgi.bundles=\\" + LINE_SEP);
        	Set<Artifact> projectArtifacts = project.getArtifacts();
        	getLog().info(projectArtifacts.size() + " artifacts.");
        	for (Artifact a : projectArtifacts) {
        		w.write("reference:file:" + bundlesDir + "/" + a.getFile().getName() + "@start,\\" + LINE_SEP);
        	}
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error creating file " + touch, e );
        }
        finally
        {
            if ( w != null )
            {
                try
                {
                    w.close();
                }
                catch ( IOException e )
                {
                    // ignore
                }
            }
        }
    }
}