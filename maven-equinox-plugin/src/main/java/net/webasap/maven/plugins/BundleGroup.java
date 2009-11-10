package net.webasap.maven.plugins;

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
