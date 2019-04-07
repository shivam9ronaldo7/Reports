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

	private ExtentHtmlReporter htmlReporter = null;
	private ExtentReports extent = null;
	private ExtentTest feature = null;
	private ExtentTest scenario = null;
	private String documentTitle = null;
	private String reportName = null;

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
			handleTestRunStarted(event);
		}
	};
	
	private EventHandler<TestSourceRead> testSourceReadHandler = new EventHandler<TestSourceRead>() {
		@Override
		public void receive(TestSourceRead event) {
			handleTestSourceRead(event);
		}
	};
	
	private EventHandler<TestCaseStarted> caseStartedHandler = new EventHandler<TestCaseStarted>() {
		@Override
		public void receive(TestCaseStarted event) {
			handleTestCaseStarted(event);
		}
	};
	
	private EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {
		@Override
		public void receive(TestStepStarted event) {
			handleTestStepStarted(event);
		}
	};

	private EventHandler<TestStepFinished> stepFinishedHandler = new EventHandler<TestStepFinished>() {
		@Override
		public void receive(TestStepFinished event) {
			handleTestStepFinished(event);
		}
	};

	private EventHandler<TestCaseFinished> caseFinishedHandler = new EventHandler<TestCaseFinished>() {
		@Override
		public void receive(TestCaseFinished event) {
			handleTestCaseFinished(event);
		}
	};

	private EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
		@Override
		public void receive(TestRunFinished event) {
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
			this.documentTitle = arr[1];
			this.reportName = arr[2];
		}		
	}

	private void handleTestRunStarted(TestRunStarted event) {
		attachExtentHtmlReporter();
		configureExtentHtmlReporter();
	}

	private void handleTestSourceRead(TestSourceRead event) {
		testSources.addTestSourceReadEvent(event.uri, event);
	}

	private void handleTestCaseStarted(TestCaseStarted event) {
		handleStartOfFeature(event.testCase);
		createTestCase(event.testCase);
	}

	private void handleTestStepStarted(TestStepStarted event) {		
	}

	private void handleTestStepFinished(TestStepFinished event) {
		if (event.testStep instanceof PickleStepTestStep) {
			PickleStepTestStep testStep = (PickleStepTestStep) event.testStep;
			createTestStep(testStep,event.result);
		}
	}

	private void handleTestCaseFinished(TestCaseFinished event) {
		endTestCase();
	}

	private void handleTestRunFinished(TestRunFinished event) {
		generateExtentHtmlReporter();
	}

	private void handleEmbed(EmbedEvent event) {}

	private void handleWrite(WriteEvent event) {}

	private void attachExtentHtmlReporter() {
		htmlReporter = new ExtentHtmlReporter(htmlReportDir);
		extent = new ExtentReports();
		extent.setSystemInfo("OS", System.getProperty("os.name"));
		extent.attachReporter(htmlReporter);
	}

	private void configureExtentHtmlReporter() {
		if(documentTitle!=null)
			htmlReporter.config().setDocumentTitle(documentTitle);
		if(reportName!=null)
			htmlReporter.config().setReportName(reportName);
		htmlReporter.config().setTheme(Theme.DARK);
		htmlReporter.config().setProtocol(Protocol.HTTPS);
		htmlReporter.config().setAutoCreateRelativePathMedia(true);
		htmlReporter.config().setCSS("css-string");
		htmlReporter.config().setEncoding("utf-8");
		htmlReporter.config().setJS("js-string");		
		htmlReporter.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
	}

	private void generateExtentHtmlReporter() {
		extent.flush();
	}

	private void createScenario(ScenarioDefinition scenarioDefinition, TestCase testCase) {		
		switch(scenarioDefinition.getKeyword()) {
		case "Scenario": scenario = feature.createNode(Scenario.class, (testCase.getName()+"\n"+
					((scenarioDefinition.getDescription() != null) ? ("\n"+scenarioDefinition.getDescription()) : "")));
			break;
		case "Scenario Outline": scenario = feature.createNode(Scenario.class, (testCase.getName()+"\n"+
				((scenarioDefinition.getDescription() != null) ? ("\n"+scenarioDefinition.getDescription()) : "")));
			break;
		default: throw new CucumberException("Wrong scenario keyword "+scenarioDefinition.getKeyword());
		}
		
	}
	
	private void createSteps(Step step, PickleStepTestStep testStep, Result result) {
		switch(step.getKeyword()) {
		case "Given ": stepStatus(scenario.createNode(Given.class, testStep.getStepText()),result);
			break;
		case "When ": stepStatus(scenario.createNode(When.class, testStep.getStepText()),result);
			break;
		case "Then ": stepStatus(scenario.createNode(Then.class, testStep.getStepText()),result);
			break;
		case "And ": stepStatus(scenario.createNode(And.class, testStep.getStepText()),result);
			break;
		default: throw new CucumberException("Wrong step keyword "+step.getKeyword());
		}
	}
	
	private void stepStatus(ExtentTest test, Result result) {
		switch(result.getStatus().toString()) {
		case "PASSED": test.pass("Pass");
			break;
		case "FAILED": test.fail("Fail");
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

	private void handleStartOfFeature(TestCase testCase) {
		if (currentFeatureFile == null || !currentFeatureFile.equals(testCase.getUri())) {
			currentFeatureFile = null;
			currentFeatureFile = testCase.getUri();
			createFeatureName(testCase.getName());
		}
	}
	
	private void createFeatureName(String currentFeatureFile) {
		feature = extent.createTest(Feature.class, currentFeatureFile);
	}

	private void createTestCase(TestCase testCase) {
		TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
		if (astNode != null) {
			ScenarioDefinition scenarioDefinition = TestSourcesModel.getScenarioDefinition(astNode);
			createScenario(scenarioDefinition, testCase);
		}
	}

	private void createTestStep(PickleStepTestStep testStep, Result result) {
		TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
		if (astNode != null) {
			Step step = (Step) astNode.node;
			createSteps(step, testStep, result);
		}
	}
	
	private void endTestCase() {
		scenario = null;
	}
}
