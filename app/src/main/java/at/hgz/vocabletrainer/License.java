package at.hgz.vocabletrainer;

public class License {

	private String moduleName;
	
	private String licenseText;

	public License(String moduleName, String licenseText) {
		this.moduleName = moduleName;
		this.licenseText = licenseText;
	}

	public String getModuleName() {
		return moduleName;
	}

	public String getLicenseText() {
		return licenseText;
	}
	
}
