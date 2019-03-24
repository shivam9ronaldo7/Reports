package runner;

import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;

@CucumberOptions(
		features = "src/test/resources",
		glue={"stepDefinition"},
		tags= {"@tag"},
		monochrome=true,
		plugin= {"com.report.CucumberExtent:target/cucumber-extent-reports/report.html"}
		)

public class TestNGRunner extends AbstractTestNGCucumberTests{}