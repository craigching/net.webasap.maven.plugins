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

public class BundleGroup {
	
	private int startLevel = 5;
	private String [] includes;
	
	public void setStartLevel (int startLevel) {
		this.startLevel = startLevel;
	}
	
	public int getStartLevel() {
		return this.startLevel;
	}
	
	public void setIncludes(String [] includes) {
		this.includes = includes;
	}
	
	public String [] getIncludes() {
		return this.includes;
	}

}
