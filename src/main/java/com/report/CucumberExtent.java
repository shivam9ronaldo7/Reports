package com.report;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
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
import gherkin.pickles.PickleTag;

public class CucumberExtent implements EventListener{

	private final TestSourcesModel testSources = new TestSourcesModel();
	private String currentFeatureFile;
	private final String htmlReportDir;
	@SuppressWarnings("serial")
	private final Map<String, String> MIME_TYPES_EXTENSIONS = new HashMap<String, String>() {
		{
			put("image/bmp", "bmp");
			put("image/gif", "gif");
			put("image/jpeg", "jpg");
			put("image/png", "png");
			put("image/svg+xml", "svg");
			put("video/ogg", "ogg");
		}
	};
	private int embeddedIndex;
	private String directory;
	private String screenPath;

	//Builds a new report using the html template
	private ExtentHtmlReporter extentHtmlReporter = null;
	private ExtentReports extent = null;
	private ExtentTest extentBDDFeature = null;
	private ExtentTest extentBDDScenario = null;
	private ExtentTest extentBDDStep = null;
	private ExtentTest extentTest = null;
	
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
			//Calling method that will be called when TestStepFinished
			try {
				handleTestStepFinished(event);
			} catch (IOException e) {
				e.printStackTrace();
			}
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
			try {
				handleEmbed(event);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	private EventHandler<WriteEvent> writeEventhandler = new EventHandler<WriteEvent>() {
		@Override
		public void receive(WriteEvent event) {
			handleWrite(event);
		}
	};

	public CucumberExtent(String htmlReportDir) {
		this.htmlReportDir = htmlReportDir;
		this.directory = getLocation(htmlReportDir);
	}

	private void handleTestRunStarted(TestRunStarted event) {
		attachExtentHtmlReporter();
		configureExtentHtmlReporter();
	}

	private void handleTestSourceRead(TestSourceRead event) {
		testSources.addTestSourceReadEvent(event.uri, event);
	}

	private void handleTestCaseStarted(TestCaseStarted event) {
		startOfFeature(event.testCase);
		if((CucumberExtentOptions.getInstance().getReportLevel()!=null)&&(CucumberExtentOptions.getInstance().getReportLevel().equals("Feature")))
			createExtentBDDTestCase(event.testCase);
		else
			createExtentTest(event.testCase);
	}

	private void handleTestStepStarted(TestStepStarted event) {		
	}

	private void handleTestStepFinished(TestStepFinished event) throws IOException {
		if (event.testStep instanceof PickleStepTestStep) {
			PickleStepTestStep testStep = (PickleStepTestStep) event.testStep;			
			if((CucumberExtentOptions.getInstance().getReportLevel()!=null)&&(CucumberExtentOptions.getInstance().getReportLevel().equals("Feature")))
				createExtentBDDTestStep(testStep,event.result);
			else
				createExtentTestStep(testStep,event.result);
		}
	}

	private void handleTestCaseFinished(TestCaseFinished event) {
		if((CucumberExtentOptions.getInstance().getReportLevel()!=null)&&(CucumberExtentOptions.getInstance().getReportLevel().equals("Feature")))
			endExtentBDDTestCase();
		else
			endExtentTestCase();
	}

	private void handleTestRunFinished(TestRunFinished event) {
		generateExtentHtmlReporter();
	}

	private void handleWrite(WriteEvent event) {}

	private void handleEmbed(EmbedEvent event) throws IOException {
		String mimeType = event.mimeType;
		String extension = MIME_TYPES_EXTENSIONS.get(mimeType);
		if (extension != null) {
			StringBuilder fileName = new StringBuilder("embedded").append(embeddedIndex++).append(Thread.currentThread().getId()).append(".").append(extension);
			convertByteArrayToImage(event.data,fileName.toString(),extension);
			this.extentBDDStep.addScreenCaptureFromPath(this.screenPath);
			this.screenPath = null;
			this.extentBDDStep = null;
		}
	}

	private void convertByteArrayToImage(byte[] byteImage, String fileName, String extension) throws IOException {
		this.screenPath = this.directory+fileName;
		ImageIO.write(ImageIO.read(new ByteArrayInputStream(byteImage)), extension, new File(this.screenPath));		
	}

	private String getLocation(String htmlReportDir) {
		String[] htmlReportDirArray = htmlReportDir.split("/");
		String dir = "";
		for(int i = 0; i<htmlReportDirArray.length-1;++i) {
			dir = (new StringBuilder(dir)).append(htmlReportDirArray[i]).append("\\").toString();
		}
		return dir;
	}

	private void attachExtentHtmlReporter() {
		extentHtmlReporter = new ExtentHtmlReporter(htmlReportDir);
		extent = new ExtentReports();
		extent.setSystemInfo("OS", System.getProperty("os.name"));
		extent.attachReporter(extentHtmlReporter);
	}

	private void configureExtentHtmlReporter() {
		if(CucumberExtentOptions.getInstance().getDocumentTitle()!=null)
			extentHtmlReporter.config().setDocumentTitle(CucumberExtentOptions.getInstance().getDocumentTitle());
		if(CucumberExtentOptions.getInstance().getReportName()!=null)
			extentHtmlReporter.config().setReportName(CucumberExtentOptions.getInstance().getReportName());
		extentHtmlReporter.config().setTheme(Theme.STANDARD);
		extentHtmlReporter.config().setProtocol(Protocol.HTTPS);
		extentHtmlReporter.config().setAutoCreateRelativePathMedia(true);
		extentHtmlReporter.config().setCSS("css-string");
		extentHtmlReporter.config().setEncoding("utf-8");
		extentHtmlReporter.config().setJS("js-string");
		extentHtmlReporter.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");
	}

	private void generateExtentHtmlReporter() {
		extent.flush();
	}

	private void createExtentBDDScenario(ScenarioDefinition scenarioDefinition, TestCase testCase) {
		switch(scenarioDefinition.getKeyword()) {
		case "Scenario": extentBDDScenario = extentBDDFeature.createNode(Scenario.class, (testCase.getName()+"\n"+
				((scenarioDefinition.getDescription() != null) ? ("\n"+scenarioDefinition.getDescription()) : "")));
		createTagList(testCase);
		break;
		case "Scenario Outline": extentBDDScenario = extentBDDFeature.createNode(Scenario.class, (testCase.getName()+"\n"+
				((scenarioDefinition.getDescription() != null) ? ("\n"+scenarioDefinition.getDescription()) : "")));
		createTagList(testCase);
		break;
		default: throw new CucumberException("Wrong scenario keyword "+scenarioDefinition.getKeyword());
		}

	}
	
	private void createExtentScenario(ScenarioDefinition scenarioDefinition, TestCase testCase) {
		switch(scenarioDefinition.getKeyword()) {
		case "Scenario": extentTest = extent.createTest(testCase.getName()+"\n"+
				((scenarioDefinition.getDescription() != null) ? ("\n"+scenarioDefinition.getDescription()) : ""));
		createTagList(testCase);
		break;
		case "Scenario Outline": extentTest = extent.createTest(testCase.getName()+"\n"+
				((scenarioDefinition.getDescription() != null) ? ("\n"+scenarioDefinition.getDescription()) : ""));
		createTagList(testCase);
		break;
		default: throw new CucumberException("Wrong scenario keyword "+scenarioDefinition.getKeyword());
		}
	}

	private void createExtentBDDSteps(Step step, PickleStepTestStep testStep, Result result) throws IOException {
		switch(step.getKeyword()) {
		case "Given ": stepStatus(extentBDDScenario.createNode(Given.class, testStep.getStepText()),result);
		break;
		case "When ": stepStatus(extentBDDScenario.createNode(When.class, testStep.getStepText()),result);
		break;
		case "Then ": stepStatus(extentBDDScenario.createNode(Then.class, testStep.getStepText()),result);
		break;
		case "And ": stepStatus(extentBDDScenario.createNode(And.class, testStep.getStepText()),result);
		break;
		default: throw new CucumberException("Wrong step keyword "+step.getKeyword());
		}
	}

	private void createExtentSteps(Step step, PickleStepTestStep testStep, Result result) throws IOException {
		switch(step.getKeyword()) {
		case "Given ": stepStatus(extentTest.createNode("Given "+testStep.getStepText()),result);
		break;
		case "When ": stepStatus(extentTest.createNode("When "+testStep.getStepText()),result);
		break;
		case "Then ": stepStatus(extentTest.createNode("Then "+testStep.getStepText()),result);
		break;
		case "And ": stepStatus(extentTest.createNode("And "+testStep.getStepText()),result);
		break;
		default: throw new CucumberException("Wrong step keyword "+step.getKeyword());
		}
	}

	private void stepStatus(ExtentTest test, Result result) throws IOException {
		this.extentBDDStep = test;
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
		test.fail(result.getErrorMessage());
		break;
		default: throw new CucumberException("Wrong step status "+result.getStatus());
		}
	}

	private void startOfFeature(TestCase testCase) {
		if (currentFeatureFile == null || !currentFeatureFile.equals(testCase.getUri())) {
			currentFeatureFile = null;
			currentFeatureFile = testCase.getUri();
			if((CucumberExtentOptions.getInstance().getReportLevel()!=null)&&(CucumberExtentOptions.getInstance().getReportLevel().equals("Feature")))
				createExtentBDDFeatureName(testCase);
		}
	}

	private void createExtentTest(TestCase testCase) {
		TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
		if (astNode != null) {
			ScenarioDefinition scenarioDefinition = TestSourcesModel.getScenarioDefinition(astNode);
			createExtentScenario(scenarioDefinition, testCase);
		}
	}

	private void createExtentBDDFeatureName(TestCase testCase) {
		gherkin.ast.Feature feature = testSources.getFeature(testCase.getUri());
		if (feature != null) {
			extentBDDFeature = extent.createTest(Feature.class, (feature.getName()+"\n"+
					((feature.getDescription() != null) ? ("\n"+feature.getDescription()) : "")));		
		}
	}

	private void createExtentBDDTestCase(TestCase testCase) {
		TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testCase.getLine());
		if (astNode != null) {
			ScenarioDefinition scenarioDefinition = TestSourcesModel.getScenarioDefinition(astNode);
			createExtentBDDScenario(scenarioDefinition, testCase);
		}
	}

	private void createExtentBDDTestStep(PickleStepTestStep testStep, Result result) throws IOException {
		TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
		if (astNode != null) {
			Step step = (Step) astNode.node;
			createExtentBDDSteps(step, testStep, result);
		}
	}

	private void createExtentTestStep(PickleStepTestStep testStep, Result result) throws IOException {
		TestSourcesModel.AstNode astNode = testSources.getAstNode(currentFeatureFile, testStep.getStepLine());
		if (astNode != null) {
			Step step = (Step) astNode.node;
			createExtentSteps(step, testStep, result);
		}
	}

	private void endExtentBDDTestCase() {
		extentBDDScenario = null;
	}

	private void endExtentTestCase() {
		extentTest = null;
	}

	private void createTagList(TestCase testCase) {
		if (!testCase.getTags().isEmpty()) {
			for (PickleTag tag : testCase.getTags()) {
				if(extentBDDScenario!=null)
					extentBDDScenario.assignAuthor(tag.getName());
				if(extentTest!=null)
					extentTest.assignAuthor(tag.getName());
			}
		}
	}

}
