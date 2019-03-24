package stepDefinition;

import cucumber.api.java8.En;

public class TestFeatures implements En{
	public TestFeatures() {
		Given("I want to write a step with precondition", () -> {});

		Given("some other precondition", () -> {});

		When("I complete action", () -> {});

		When("some other action", () -> {});

		When("yet another action", () -> {});

		Then("I validate the outcomes", () -> {});

		Then("check more outcomes", () -> {});

		Given("I want to write a step with name{int}", (Integer int1) -> {});

		When("I check for the {int} in step", (Integer int1) -> {});

		Then("I verify the success in step", () -> {});

		Then("I verify the Fail in step", () -> {});
	}
}
