package com.report;

public class CucumberExtentOptions {
	
	private static ThreadLocal<CucumberExtentOptions> cucumberExtentOptions = new ThreadLocal<CucumberExtentOptions>();
	private String documentTitle = null;
	private String reportName = null;
	private String reportLevel = null;
	
	private CucumberExtentOptions() {}
	
	public static CucumberExtentOptions getInstance() {
		if(cucumberExtentOptions.get() == null)
			cucumberExtentOptions.set(new CucumberExtentOptions());
		return cucumberExtentOptions.get();
	}
	
	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}
	
	public String getDocumentTitle() {
		return this.documentTitle;
	}
	
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	public String getReportName() {
		return this.reportName;
	}
	
	public void setReportLevel(String reportLevel) {
		this.reportLevel = reportLevel;
	}
	
	public String getReportLevel() {
		return this.reportLevel;
	}
	
}
