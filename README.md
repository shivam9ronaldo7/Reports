# CucumberExtent Report
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.shivam9ronaldo7/cucumber-extent/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.rsql/rsql-parser)

- [Cucumber Extent Report](https://github.com/shivam9ronaldo7/cucumber-extent) is a Cucumber-Extent report generation jar. It will help to generate Extent Report with Cucumber as BDD tool.
- [Cucumber Extent Report](https://github.com/shivam9ronaldo7/cucumber-extent) is developed for user to easily generate Extent Report with Cucumber.

Note:
[Cucumber](https://cucumber.io/) is developed by Cucumber Team.
[Extent Reporting Framework](http://extentreports.relevantcodes.com/) is developed by Anshoo Arora.

## Usage
Please add below mentioned dependency to your pom.xml

```
<dependency>
  <groupId>com.github.shivam9ronaldo7</groupId>
  <artifactId>cucumber-extent</artifactId>
  <version>1.0.1</version>
</dependency>
```

**Please use JAVA 8+, Extent 4.0.7+ & Cucumber-JVM 4.2.0+**

If you are not using maven, please download the jar from [here](https://search.maven.org/).

## Implementation
-Create a runner class and add the `com.report.CucumberExtent:<Report-Location>/<Report-Name>.html` as a plugin in CucumberOptions.
-If you want to set report tile use `CucumberExtentOptions.getInstance().setDocumentTitle("My Report");` command.
-If you want to set report name use `CucumberExtentOptions.getInstance().setReportName("Report Name");` command.
-By default report is available at Scenario level. But if you want report at Feature level use `CucumberExtentOptions.getInstance().setReportLevel("Feature");`

Runner Sample

```
import org.testng.annotations.BeforeClass;
import com.report.CucumberExtentOptions;
import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;

@CucumberOptions(
		features = "src/test/resources",
		glue={"stepDefinition"},
		tags= {"@ftag1"},
		monochrome=true,
		plugin= {"com.report.CucumberExtent:target/cucumber-extent-reports/report.html"}
		)
public class TestNGRunner extends AbstractTestNGCucumberTests{
	@BeforeClass
	public void beforeMethod() {
		CucumberExtentOptions.getInstance().setDocumentTitle("Shivam document title");
		CucumberExtentOptions.getInstance().setReportLevel("Feature");
		CucumberExtentOptions.getInstance().setReportName("Shivam report name");		
	}
}
```
## Adding Screenshot To Report
Screenshots at only failed test steps:

```
@AfterStep
public void afterStepHook(Scenario scenario) throws IOException {
	if(scenario.isFailed()==true) {
		scenario.embed(((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES), "image/png");
	}
}
```

Screenshots at all test steps:
```
@AfterStep
public void afterStepHook(Scenario scenario) throws IOException {
	scenario.embed(((TakesScreenshot)driver).getScreenshotAs(OutputType.BYTES), "image/png");
}
```

## This report will work with Cucumbers parallel execution.
To know about cucumber parallel execution click [here](https://github.com/cucumber/cucumber-jvm/tree/master/testng).
Feel free to report any issue if faced.
