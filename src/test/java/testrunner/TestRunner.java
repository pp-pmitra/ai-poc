package testrunner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {"src/test/resources/features/life/Life_Pixels.feature"},
        glue = {"stepdefinitions", "hooks"},
        plugin = {
            "pretty",
            "html:target/cucumber-reports/report.html",
            "json:target/cucumber-reports/cucumber.json",
            "junit:target/cucumber-reports/Cucumber.xml",
            "rerun:target/failed_scenarios.txt",
            "hooks.StepTracingPlugin"
        })
public class TestRunner {}
