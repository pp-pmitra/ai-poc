"""Tests for offline_dom_analyzer — deterministic DOM-based failure diagnosis."""

from __future__ import annotations

import sys
import tempfile
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from failure_analyzer.analyzers.offline_dom_analyzer import (
    analyze_offline,
    extract_selector,
)

SAMPLE_HTML = """\
<html>
<head><title>Test Page</title></head>
<body>
  <div id="main">
    <h1>Campaign Details</h1>
    <form>
      <label for="name-input">Campaign Name</label>
      <input id="name-input" type="text" placeholder="Enter campaign name" value="Auto Campaign" />
      <div class="button-row">
        <button type="submit" role="button">Save Draft</button>
        <button type="button" role="button" disabled>Publish</button>
      </div>
    </form>
    <div class="error" style="display:none">Something went wrong</div>
    <div class="alert">Session will expire in 5 minutes</div>
    <span aria-hidden="true" class="hidden-text">Save</span>
  </div>
</body>
</html>
"""


@pytest.fixture
def html_artifact(tmp_path):
    html_file = tmp_path / "page-source.html"
    html_file.write_text(SAMPLE_HTML, encoding="utf-8")
    screenshot = tmp_path / "screenshot.png"
    screenshot.write_bytes(b"fake-png")
    return {
        "pageSource": str(html_file),
        "screenshot": str(screenshot),
    }


class TestExtractSelector:
    def test_xpath_selector(self):
        log = "locator('//button[contains(text(),\"Save\")]')"
        result = extract_selector(log)
        assert result is not None
        assert result["type"] == "xpath"
        assert "Save" in result["value"]

    def test_text_selector(self):
        log = "locator('text=Submit')"
        result = extract_selector(log)
        assert result is not None
        assert result["type"] == "text"
        assert result["value"] == "Submit"

    def test_getby_text(self):
        log = "getByText('Save Draft')"
        result = extract_selector(log)
        assert result is not None
        assert result["type"] == "text"
        assert result["value"] == "Save Draft"

    def test_getby_role(self):
        log = "getByRole('button', {name: 'Save'})"
        result = extract_selector(log)
        assert result is not None
        assert result["type"] == "role"
        assert result["role"] == "button"
        assert result["name"] == "Save"

    def test_getby_label(self):
        log = "getByLabel('Campaign Name')"
        result = extract_selector(log)
        assert result is not None
        assert result["type"] == "label"
        assert result["value"] == "Campaign Name"

    def test_css_selector(self):
        log = "locator('#name-input')"
        result = extract_selector(log)
        assert result is not None
        assert result["type"] == "css"
        assert result["value"] == "#name-input"

    def test_waiting_for_selector(self):
        log = 'waiting for selector "//div[@id=\'main\']"'
        result = extract_selector(log)
        assert result is not None
        assert result["type"] == "xpath"

    def test_none_on_empty(self):
        assert extract_selector(None) is None
        assert extract_selector("") is None


class TestAnalyzeOffline:
    def test_returns_none_without_artifacts(self):
        assert analyze_offline({"scenarioName": "test"}) is None

    def test_stale_xpath_selector(self, html_artifact):
        failure = {
            "scenarioName": "Test scenario",
            "failureArtifacts": html_artifact,
            "playwrightCallLog": "locator('//button[contains(text(),\"Delete\")]')",
            "errorMessage": "Timeout waiting for selector",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert result["analysisTier"] == "tier2_offline_dom"
        assert result["verdict"] in ("script_issue_fix_proposed", "needs_investigation")
        assert any("0 node" in e for e in result["evidence"])

    def test_matching_xpath_selector(self, html_artifact):
        failure = {
            "scenarioName": "Test scenario",
            "failureArtifacts": html_artifact,
            "playwrightCallLog": "locator('//button[contains(text(),\"Save Draft\")]')",
            "errorMessage": "Timeout waiting for selector",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert any("1 node" in e for e in result["evidence"])
        assert any("visible and enabled" in e for e in result["evidence"])

    def test_disabled_element(self, html_artifact):
        failure = {
            "scenarioName": "Test scenario",
            "failureArtifacts": html_artifact,
            "playwrightCallLog": "locator('//button[contains(text(),\"Publish\")]')",
            "errorMessage": "Element is disabled",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert any("DISABLED" in e for e in result["evidence"])

    def test_text_found_elsewhere(self, html_artifact):
        failure = {
            "scenarioName": "Test scenario",
            "failureArtifacts": html_artifact,
            "playwrightCallLog": "locator('//a[text()=\"Save\"]')",
            "errorMessage": "Timeout",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert any("found elsewhere" in e.lower() or "alternative" in e.lower() for e in result["evidence"])

    def test_role_selector(self, html_artifact):
        failure = {
            "scenarioName": "Test scenario",
            "failureArtifacts": html_artifact,
            "playwrightCallLog": "getByRole('button', {name: 'Save Draft'})",
            "errorMessage": "Timeout",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert any("1 node" in e for e in result["evidence"])

    def test_label_selector(self, html_artifact):
        failure = {
            "scenarioName": "Test scenario",
            "failureArtifacts": html_artifact,
            "playwrightCallLog": "getByLabel('Campaign Name')",
            "errorMessage": "Timeout",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert any("1 node" in e or "matched" in e.lower() for e in result["evidence"])

    def test_assertion_expected_actual(self, html_artifact):
        failure = {
            "scenarioName": "Test scenario",
            "failureArtifacts": html_artifact,
            "playwrightCallLog": None,
            "errorMessage": "expected 'Draft Campaign' but was 'Auto Campaign'",
            "expectedValue": "Draft Campaign",
            "actualValue": "Auto Campaign",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert any("actual" in e.lower() and "present" in e.lower() for e in result["evidence"])

    def test_visible_error_detection(self, html_artifact):
        failure = {
            "scenarioName": "Test scenario",
            "failureArtifacts": html_artifact,
            "playwrightCallLog": "locator('//button[text()=\"Missing\"]')",
            "errorMessage": "Timeout",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert any("alert" in e.lower() or "error" in e.lower() for e in result["evidence"])


class TestShadowDomLocator:
    """Failures where the target text lives inside a serialized shadow-root
    comment block (Stencil.js / ds-* components).  XPath cannot pierce shadow
    DOM; the offline analyzer must detect this from the raw HTML and propose
    a concrete aria-label-based replacement without escalating to live replay."""

    SHADOW_HTML = """<html><body>
  <div data-tour-id="filters-drawer">
    <div class="sc-dExYaf evVeKq">
      <ds-toggle class="hydrated"><!--[shadow-root]--><div class="inline-flex items-center gap-s cursor-pointer"><button type="button" role="switch" aria-checked="false" aria-label="35 to 45" class="relative bg-transparent border-none p-0 cursor-pointer rounded-full"></button><span part="label" class="text-text-primary"><slot>35 to 45</slot></span></div><!--[/shadow-root]--></ds-toggle>
    </div>
    <div class="sc-dExYaf evVeKq">
      <ds-toggle class="hydrated"><!--[shadow-root]--><div class="inline-flex items-center gap-s cursor-pointer"><button type="button" role="switch" aria-checked="false" aria-label="45 to 55" class="relative bg-transparent border-none p-0 cursor-pointer rounded-full"></button><span part="label"><slot>45 to 55</slot></span></div><!--[/shadow-root]--></ds-toggle>
    </div>
  </div>
</body></html>
"""

    @pytest.fixture
    def shadow_artifact(self, tmp_path):
        html_file = tmp_path / "page-source.html"
        html_file.write_text(self.SHADOW_HTML, encoding="utf-8")
        return {"pageSource": str(html_file), "screenshot": None}

    def test_xpath_label_contains_resolves_without_live_replay(self, shadow_artifact):
        """//label[contains(text(),'35 to 45')] — the canonical failing locator."""
        failure = {
            "scenarioName": "Create HCP Explorer Workspace",
            "failureArtifacts": shadow_artifact,
            "playwrightCallLog": (
                "- waiting for locator(\"iframe\").contentFrame()"
                ".locator(\"iframe\").contentFrame()"
                ".locator(\"//label[contains(text(),'35 to 45')]\") to be visible"
            ),
            "errorMessage": "Timeout 120000ms exceeded.",
        }
        result = analyze_offline(failure)
        assert result is not None, "analyze_offline returned None — no artifacts path"
        assert result["verdict"] == "script_issue_fix_proposed", (
            f"Expected script_issue_fix_proposed, got {result['verdict']}. "
            f"Evidence: {result['evidence']}"
        )
        assert result["confidence"] == "high"
        assert result["diagnosisKind"] == "shadow_dom_locator"
        assert any("shadow" in e.lower() for e in result["evidence"])
        assert any("ds-toggle" in e or "aria-label" in e for e in result["evidence"])

    def test_xpath_normalize_space_also_resolves(self, shadow_artifact):
        """//label[normalize-space(.)='35 to 45'] — the second attempt that also failed."""
        failure = {
            "scenarioName": "Create HCP Explorer Workspace",
            "failureArtifacts": shadow_artifact,
            "playwrightCallLog": (
                "- waiting for locator(\"iframe\").contentFrame()"
                ".locator(\"iframe\").contentFrame()"
                ".locator(\"//label[normalize-space(.)='35 to 45']\") to be visible"
            ),
            "errorMessage": "Timeout 120000ms exceeded.",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert result["verdict"] == "script_issue_fix_proposed"
        assert result["diagnosisKind"] == "shadow_dom_locator"

    def test_suggested_selector_contains_aria_label(self, shadow_artifact):
        """The suggested selector must include the aria-label value from the shadow DOM button."""
        failure = {
            "scenarioName": "Create HCP Explorer Workspace",
            "failureArtifacts": shadow_artifact,
            "playwrightCallLog": "locator(\"//label[contains(text(),'35 to 45')]\") timeout",
            "errorMessage": "Timeout",
        }
        result = analyze_offline(failure)
        assert result is not None
        evidence_text = " ".join(result["evidence"])
        assert "35 to 45" in evidence_text
        # Suggested selector must reference aria-label
        assert "aria-label" in evidence_text

    def test_wrong_text_not_in_shadow_still_escalates(self, shadow_artifact):
        """Text that does not exist anywhere — neither lxml nor shadow — stays needs_investigation."""
        failure = {
            "scenarioName": "Create HCP Explorer Workspace",
            "failureArtifacts": shadow_artifact,
            "playwrightCallLog": "locator(\"//label[contains(text(),'99 to 100')]\") timeout",
            "errorMessage": "Timeout",
        }
        result = analyze_offline(failure)
        # Either None (no selector extracted) or needs_investigation — never a fix
        if result is not None:
            assert result["verdict"] != "script_issue_fix_proposed", (
                "Should not propose a fix for text that is not in the DOM at all"
            )


class TestInvisibleCharTextMismatch:
    """A design-system change appends an invisible/zero-width character right
    after a label (e.g. word-joiner U+2060 for a tooltip/icon hook). The panel
    holding the label may live in a CDK overlay that never makes it into the
    saved page-source.html, so page-source.html alone shows 0 matches and 0
    occurrences of the text anywhere — but the separately-captured pageText
    (a plain innerText snapshot) does have it, invisible character and all.
    An exact-match XPath predicate (text()='X' / normalize-space(...)='X')
    can never equal "X\\u2060", even though the label is visibly correct."""

    @pytest.fixture
    def empty_artifact(self, tmp_path):
        html_file = tmp_path / "page-source.html"
        html_file.write_text("<html><body></body></html>", encoding="utf-8")
        return {"pageSource": str(html_file), "screenshot": None}

    def test_normalize_space_exact_match_flags_invisible_char(self, empty_artifact):
        failure = {
            "scenarioName": "Create a Campaign with multiple Targeting Rules added to a Tactic",
            "failureArtifacts": empty_artifact,
            "playwrightCallLog": "locator(\"//button[normalize-space(text())='Household']\") timeout",
            "errorMessage": "Timeout 120000ms exceeded.",
            "pageText": "Device⁠ Person⁠ Household⁠ Household IP⁠ HISTORICAL PERIOD",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert result["verdict"] == "script_issue_fix_proposed"
        assert result["confidence"] == "high"
        assert result["diagnosisKind"] == "invisible_char_text_mismatch"
        evidence_text = " ".join(result["evidence"])
        assert "U+2060" in evidence_text
        assert "translate" in result["recommendedAction"]

    def test_exact_text_predicate_also_flags_invisible_char(self, empty_artifact):
        failure = {
            "scenarioName": "Test scenario",
            "failureArtifacts": empty_artifact,
            "playwrightCallLog": "locator(\"//button[text()='Upload']\") timeout",
            "errorMessage": "Timeout",
            "pageText": "Some text Upload​ more text",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert result["diagnosisKind"] == "invisible_char_text_mismatch"
        assert result["verdict"] == "script_issue_fix_proposed"

    def test_contains_predicate_not_misdiagnosed_as_invisible_char(self, empty_artifact):
        """contains(text(),'X') still matches 'X\\u2060' as a substring, so a
        genuine 0-match failure with a contains() predicate is NOT this bug —
        must not be misclassified as invisible_char_text_mismatch."""
        failure = {
            "scenarioName": "Test scenario",
            "failureArtifacts": empty_artifact,
            "playwrightCallLog": "locator(\"//button[contains(text(),'Household')]\") timeout",
            "errorMessage": "Timeout",
            "pageText": "Household⁠ Household IP⁠",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert result["diagnosisKind"] != "invisible_char_text_mismatch"

    def test_no_invisible_char_no_false_positive(self, empty_artifact):
        """Text genuinely absent, no invisible character present — must stay
        the ordinary element_absent/needs_investigation path."""
        failure = {
            "scenarioName": "Test scenario",
            "failureArtifacts": empty_artifact,
            "playwrightCallLog": "locator(\"//button[normalize-space(text())='Household']\") timeout",
            "errorMessage": "Timeout",
            "pageText": "Nothing relevant on this page",
        }
        result = analyze_offline(failure)
        assert result is not None
        assert result["diagnosisKind"] != "invisible_char_text_mismatch"



if __name__ == "__main__":
    raise SystemExit(pytest.main([__file__]))
