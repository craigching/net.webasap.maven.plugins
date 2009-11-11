package net.webasap.maven.plugins;

/*
 * Copyright 2009 Craig L. Ching
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
import java.io.Writer;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

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
	final static private String SYSTEM_PKGS = "org.osgi.framework.system.packages";
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
    
    private void resolveBundles(Set<Artifact> artifacts) {
    	for (BundleGroup bg : this.bundleGroups) {
    		getLog().info("====== Bundle Group: Start Level: " + bg.getStartLevel());
    		String [] includes = bg.getIncludes();
    		for (String i : includes) {
    			getLog().info("Bundle: " + i);
    			String [] s = i.split(":");
    			String groupId = s[0];
    			String artifactId = s[1];
    			Artifact found = null;
    			for (Artifact a : artifacts) {
    				if (a.getArtifactId().equals(artifactId) && a.getGroupId().equals(groupId)) {
    					found = a;
    					break;
    				}
    			}
    			
    			if (found != null) {
    				artifacts.remove(found);
    				bg.addArtifact(found);
    			} else {
    				getLog().error("No such artifact in project dependencies: " + i);
    			}
    		}
    		getLog().info("=================");
    	}
    }
    
    private String getBundleReference(File f, int sl) {
    	StringBuilder buf = new StringBuilder();
		buf.append("reference:file:")
		.append(bundlesDir)
		.append("/")
		.append(f.getName());
		
		try {
			if (!isFragment(f)) {
				buf.append("@");
				if (sl > 0) {
					buf.append(sl).append(":");
				}
				buf.append("start");
			}
		} catch (IOException e) {
		}
		return buf.toString();
    }
    
    public void execute()
        throws MojoExecutionException
    {

    	Set<Artifact> projectArtifacts = project.getArtifacts();
    	resolveBundles(projectArtifacts);
        File f = outputDirectory;

        if ( !f.exists() )
        {
            f.mkdirs();
        }

        File configFile = new File( f, "config.ini" );
        
        Writer w = null;
        try
        {
            w = new FileWriter( configFile );
            
            StringBuilder buf = new StringBuilder();
            List<String> bundleRefs = new LinkedList<String>();

        	Enumeration<?> p = properties.propertyNames();
        	while(p.hasMoreElements()) {
        		String k = (String)p.nextElement();
        		String v = properties.getProperty(k);
        		// Ignore the system packages property for now
        		// we want to put it in its own section
        		if (!k.equals(SYSTEM_PKGS)) {
        			buf.append(k).append("=").append(v).append(LINE_SEP);
        		}
        	}
        	
        	// Handle the system packages now
        	buf.append(LINE_SEP).append("### System Packages ###").append(LINE_SEP);
        	buf.append(SYSTEM_PKGS).append("=").append(properties.getProperty(SYSTEM_PKGS)).append(LINE_SEP);
        	
        	buf.append(LINE_SEP).append("### Bundles ###").append(LINE_SEP);
        	buf.append("osgi.bundles=\\").append(LINE_SEP);
        	getLog().info(projectArtifacts.size() + " artifacts.");
        	for (BundleGroup bg : bundleGroups) {
        		int startLevel = bg.getStartLevel();
        		List<Artifact> artifacts = bg.getArtifacts();
        		for (Artifact a : artifacts) {
        			bundleRefs.add(getBundleReference(a.getFile(), startLevel));
        		}
        	}
        	for (Artifact a : projectArtifacts) {
        		bundleRefs.add(getBundleReference(a.getFile(), -1));
        	}
        	
        	int num_refs = bundleRefs.size();
        	for (String ref : bundleRefs) {
        		buf.append(ref);
        		if (--num_refs > 0) {
        			buf.append(",\\" + LINE_SEP);
        		}
        	}
        	
        	w.write(buf.toString());
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error creating file " + configFile, e );
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

    final static private boolean isFragment(File f) throws IOException {
    	boolean isFragment = false;
    	JarFile j = new JarFile(f);
    	Manifest m = j.getManifest();
    	if (m != null) {
    		Attributes attrs = m.getMainAttributes();
    		isFragment = attrs.containsKey("Fragment-Host");
    	}
    	return isFragment;
    }
}
