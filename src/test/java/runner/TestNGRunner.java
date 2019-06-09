package runner;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import com.report.CucumberExtentOptions;
import cucumber.api.CucumberOptions;
import cucumber.api.testng.AbstractTestNGCucumberTests;

@CucumberOptions(
		features = "src/test/resources",
		glue={"stepDefinition"},
		tags= {/*"@tag",*/"@ftag1"},
		monochrome=true,
		plugin= {/*"html:target/cucumber-report"*/
				"com.report.CucumberExtent:target/cucumber-extent-reports/report.html"
				}
		)

public class TestNGRunner extends AbstractTestNGCucumberTests{
	/*@Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }*/
	@BeforeClass
	public void beforeMethod() {
		CucumberExtentOptions.getInstance().setDocumentTitle("My document title");
		//CucumberExtentOptions.getInstance().setReportLevel("Feature");
		CucumberExtentOptions.getInstance().setReportName("My report name");		
	}
}