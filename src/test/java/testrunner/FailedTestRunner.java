package testrunner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        // Rerun failed tests from failed_scenarios.txt file
        features = {"@target/failed_scenarios.txt"},
        glue = {"stepdefinitions", "hooks"},
        tags = "@e2e",
        plugin = {
            "pretty",
            "html:target/cucumber-reports/report.html",
            "json:target/cucumber-reports/cucumber.json",
            "junit:target/cucumber-reports/Cucumber.xml",
            "hooks.StepTracingPlugin"
        })
public class FailedTestRunner {}
