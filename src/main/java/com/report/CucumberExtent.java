package com.report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.gherkin.model.And;
import com.aventstack.extentreports.gherkin.model.Feature;
import com.aventstack.extentreports.gherkin.model.Given;
import com.aventstack.extentreports.gherkin.model.Scenario;
import com.aventstack.extentreports.gherkin.model.Then;
import com.aventstack.extentreports.gherkin.model.When;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Protocol;
import com.aventstack.extentreports.reporter.configuration.Theme;

import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.event.EmbedEvent;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.api.event.TestSourceRead;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.api.event.WriteEvent;
import cucumber.runtime.CucumberException;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;

public class CucumberExtent implements EventListener{

	private final TestSourcesModel testSources = new TestSourcesModel();
	private String currentFeatureFile;
	private final String htmlReportDir;

	//Builds a new report using the html template
	private ExtentHtmlReporter extentHtmlReporter = null;
	private ExtentReports extent = null;
	private ExtentTest extentFeature = null;
	private ExtentTest extentScenario = null;
	private String extentDocumentTitle = null;
	private String extentReportName = null;

	public void setEventPublisher(EventPublisher publisher) {
		publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
		publisher.registerHandlerFor(TestRunStarted.class, runStartedHandler);
		publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
		publisher.registerHandlerFor(TestCaseStarted.class, caseStartedHandler);
		publisher.registerHandlerFor(TestCaseFinished.class, caseFinishedHandler);
		publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
		publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
		publisher.registerHandlerFor(EmbedEvent.class, embedEventhandler);
		publisher.registerHandlerFor(WriteEvent.class, writeEventhandler);
	}

	private EventHandler<TestRunStarted> runStartedHandler = new EventHandler<TestRunStarted>() {
		@Override
		public void receive(TestRunStarted event) {
			//Calling method that will be called when TestRunStarted
			handleTestRunStarted(event);
		}
	};

	private EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
		@Override
		public void receive(TestSourceRead event) {
			//Calling method to read feature file
			handleTestSourceRead(event);
		}
	};

	private EventHandler<TestCaseStarted> caseStartedHandler = new EventHandler<TestCaseStarted>() {
		@Override
		public void receive(TestCaseStarted event) {
			//Calling method that will be called when TestCaseStarted
			handleTestCaseStarted(event);
		}
	};

	private EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {
		@Override
		public void receive(TestStepStarted event) {
			//Calling method that will be called when TestStepStarted
			handleTestStepStarted(event);
		}
	};

	private EventHandler<TestStepFinished> stepFinishedHandler = new EventHandler<TestStepFinished>() {
		@Override
		public void receive(TestStepFinished event) {
			//Calling method that will be called when TestStepFinished
			handleTestStepFinished(event);
		}
	};

	private EventHandler<TestCaseFinished> caseFinishedHandler = new EventHandler<TestCaseFinished>() {
		@Override
		public void receive(TestCaseFinished event) {
			//Calling method that will be called when TestCaseFinished
			handleTestCaseFinished(event);
		}
	};

	private EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
		@Override
		public void receive(TestRunFinished event) {
			//Calling method that will be called when TestRunFinished
			handleTestRunFinished(event);
		}
	};

	private EventHandler<EmbedEvent> embedEventhandler = new EventHandler<EmbedEvent>() {
		@Override
		public void receive(EmbedEvent event) {
			handleEmbed(event);
		}
	};

	private EventHandler<WriteEvent> writeEventhandler = new EventHandler<WriteEvent>() {
		@Override
		public void receive(WriteEvent event) {
			handleWrite(event);
		}
	};

	public CucumberExtent(String htmlReportDir) {
		String[] arr = htmlReportDir.split(";");
		this.htmlReportDir = arr[0];
		if(arr.length>1) {
			this.extentDocumentTitle = arr[1];
			this.extentReportName = arr[2];
		}		
	}

	//Called at the beginning of the test run
	private void handleTestRunStarted(TestRunStarted event) {
		attachExtentHtmlReporter();
		configureExtentHtmlReporter();
	}

	//Called when the contents of a new feature file have been read
	private void handleTestSourceRead(TestSourceRead event) {
		testSources.addTestSourceReadEvent(event.uri, event);
	}

	//Called at the beginning of the test case
	private void handleTestCaseStarted(TestCaseStarted event) {
		handleStartOfFeature(event.testCase);
		createTestCase(event.testCase);
	}

	//Called at the beginning of the test step
	private void handleTestStepStarted(TestStepStarted event) {		
	}

	//Called at the end of the test step
	private void handleTestStepFinished(TestStepFinished event) {
		if (event.testStep instanceof PickleStepTestStep) {
			PickleStepTestStep testStep = (PickleStepTestStep) event.testStep;
			createTestStep(testStep,event.result);
		}
	}

	//Called at the end of the test case
	private void handleTestCaseFinished(TestCaseFinished event) {
		endTestCase();
	}

	//Called at the end of the test run
	private void handleTestRunFinished(TestRunFinished event) {
		generateExtentHtmlReporter();
	}

	private void handleEmbed(EmbedEvent event) {}

	private void handleWrite(WriteEvent event) {}

	//Method to attach ExtentHtmlReporter
	private void attachExtentHtmlReporter() {
		//Initialize the HtmlReporter
		extentHtmlReporter = new ExtentHtmlReporter(htmlReportDir);
		//Initialize ExtentReports and attach the HtmlReporter
		extent = new ExtentReports();
		extent.setSystemInfo("OS", System.getProperty("os.name"));
		extent.attachReporter(extentHtmlReporter);
	}

	//Method to configure ExtentHtmlReporter
	private void configureExtentHtmlReporter() {
		//Configuration items to change the look and feel
		if(extentDocumentTitle!=null)
			extentHtmlReporter.config().setDocumentTitle(extentDocumentTitle);
		if(extentReportName!=null)
			extentHtmlReporter.config().setReportName(extentReportName);
		extentHtmlReporter.config().setTheme(Theme.DARK);
		extentHtmlReporter.config().setProtocol(Protocol.HTTPS);
		extentHtmlReporter.config().setAutoCreateRelativePathMedia(true);
		extentHtmlReporter.config().setCSS("css-string");
		extentHtmlReporter.config().setEncoding("utf-8");
		extentHtmlReporter.config().setJS("js-string");		
		extentHtmlReporter.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
	}

	//Method to generate ExtentHtmlReporter
	private void generateExtentHtmlReporter() {
		//To write or update test information to reporter
		extent.flush();
	}

	//Method to create Scenario or Scenario Outline
	private void createScenario(ScenarioDefinition scenarioDefinition, TestCase testCase) {		
		switch(scenarioDefinition.getKeyword()) {
		case "Scenario": extentScenario = extentFeature.createNode(Scenario.class, (testCase.getName()+"\n"+
				((scenarioDefinition.getDescription() != null) ? ("\n"+scenarioDefinition.getDescription()) : "")));
		break;
		case "Scenario Outline": extentScenario = extentFeature.createNode(Scenario.class, (testCase.getName()+"\n"+
				((scenarioDefinition.getDescription() != null) ? ("\n"+scenarioDefinition.getDescription()) : "")));
		break;
		default: throw new CucumberException("Wrong scenario keyword "+scenarioDefinition.getKeyword());
		}

	}

	//Method to create Steps
	private void createSteps(Step step, PickleStepTestStep testStep, Result result) {
		switch(step.getKeyword()) {
		case "Given ": stepStatus(extentScenario.createNode(Given.class, testStep.getStepText()),result);
		break;
		case "When ": stepStatus(extentScenario.createNode(When.class, testStep.getStepText()),result);
		break;
		case "Then ": stepStatus(extentScenario.createNode(Then.class, testStep.getStepText()),result);
		break;
		case "And ": stepStatus(extentScenario.createNode(And.class, testStep.getStepText()),result);
		break;
		default: throw new CucumberException("Wrong step keyword "+step.getKeyword());
		}
	}

	//Method to select type of Steps
	private void stepStatus(ExtentTest test, Result result) {
		switch(result.getStatus().toString()) {
		case "PASSED": test.pass("Pass");
		break;
		case "FAILED": test.fail("Fail");
		test.fail(result.getErrorMessage());
		break;
		case "SKIPPED": test.skip("Skip");
		break;
		case "UNDEFINED": test.fatal("Undefined");
		break;
		case "PENDING": test.warning("Pending");
		break;
		default: throw new CucumberException("Wrong step status "+result.getStatus());
		}
	}

	//Method to store address of Feature file
	private void handleStartOfFeature(TestCase testCase) {
		if (currentFeatureFile == null || !currentFeatureFile.equals(testCase.getUri())) {
			currentFeatureFile = null;
			currentFeatureFile = testCase.getUri();
			createFeatureName(testCase);
		}
	}

	//Method to create Feature name
	private void createFeatureName(TestCase testCase) {
		gherkin.ast.Feature feature = testSources.getFeature(testCase.getUri());
		if (feature != null) {
			extentFeature = extent.createTest(Feature.class, (feature.getName()+"\n"+
					((feature.getDescription() != null) ? ("\n"+feature.getDescription()) : "")));
		}
	}

	//Method to create Scenario name
	private void createTestCase(TestCase testCase) {
		TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
		if (astNode != null) {
			ScenarioDefinition scenarioDefinition = TestSourcesModel.getScenarioDefinition(astNode);
			createScenario(scenarioDefinition, testCase);
		}
	}

	//Method to create Test Step
	private void createTestStep(PickleStepTestStep testStep, Result result) {
		TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
		if (astNode != null) {
			Step step = (Step) astNode.node;
			createSteps(step, testStep, result);
		}
	}

	//Method to end Scenario
	private void endTestCase() {
		extentScenario = null;
	}
}