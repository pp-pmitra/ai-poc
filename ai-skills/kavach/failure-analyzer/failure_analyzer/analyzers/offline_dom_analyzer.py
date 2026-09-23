"""Offline DOM analyzer — deterministic failure diagnosis from saved page source.

Parses the HTML captured at failure time (page-source.html) and evaluates the
failed Playwright selector against it.  No LLM calls, no live browser.

Public API:
    analyze_offline(failure_dict) -> dict | None
        Returns a compact receipt dict when artifacts exist and analysis is
        possible, None otherwise.
"""

from __future__ import annotations

import re
from pathlib import Path
from typing import Any, Optional

from lxml import html as lxml_html
from lxml import etree


# ---------------------------------------------------------------------------
# Selector extraction from Playwright call log
# ---------------------------------------------------------------------------

_LOCATOR_RE = re.compile(
    r"""locator\((['"])(.*?)\1\)|waiting for (?:selector|locator)\s+(['"])(.*?)\3""",
    re.IGNORECASE | re.DOTALL,
)

_GETBY_TEXT_RE = re.compile(r"getByText\(['\"](.+?)['\"]\s*\)", re.IGNORECASE)
_GETBY_ROLE_RE = re.compile(
    r"getByRole\(['\"](\w+)['\"](?:,\s*\{[^}]*name:\s*['\"](.+?)['\"])?",
    re.IGNORECASE,
)
_GETBY_LABEL_RE = re.compile(r"getByLabel\(['\"](.+?)['\"]\s*\)", re.IGNORECASE)
_GETBY_PLACEHOLDER_RE = re.compile(r"getByPlaceholder\(['\"](.+?)['\"]\s*\)", re.IGNORECASE)

_TEXT_PREFIX_RE = re.compile(r"^text=(.+)", re.IGNORECASE)
# Captures XPath text predicates — groups: 1=text() comma form, 2=text()= form,
# 3=normalize-space()= form, 4=contains(text(),...) form
# The normalize-space(...) argument allows one level of nested parens (e.g.
# `normalize-space(text())=`) — a bare [^)]* stops at the inner `)` from
# `text()` and never reaches the outer `=`, silently failing to extract the
# text target for that (very common) locator shape.
_XPATH_TEXT_RE = re.compile(
    r"""(?:text\(\)\s*,\s*['"](.+?)['"]"""
    r"""|text\(\)\s*=\s*['"](.+?)['"]"""
    r"""|normalize-space\((?:[^()]|\([^()]*\))*\)\s*=\s*['"](.+?)['"]"""
    r"""|contains\(\s*text\(\)\s*,\s*['"](.+?)['"]\s*\))""",
    re.IGNORECASE,
)
# Extracts class name from contains(@class,'X') predicates
_XPATH_CLASS_RE = re.compile(r"""contains\(@class,\s*['"]([^'"]+)['"]\)""", re.IGNORECASE)

# Zero-width / invisible codepoints that a design-system component sometimes
# appends right after a visible label (icon hooks, tooltip anchors, a11y
# markers). normalize-space() only strips ASCII whitespace, so an exact-match
# XPath text predicate (text()='X', normalize-space(...)='X') never equals
# "X<one of these>" even though the label reads correctly on screen. A
# contains()-style predicate is unaffected since "X" is still a substring.
_INVISIBLE_CHARS = "​‌‍⁠﻿­᠎"


def _extract_xpath_text_and_kind(xpath: str) -> tuple[str | None, str | None]:
    """Extract an XPath text-predicate target and classify it as "exact"
    (text()='X', normalize-space(...)='X') or "contains" (contains(text(),'X')).
    Only "exact" predicates are vulnerable to trailing invisible characters.
    """
    m = _XPATH_TEXT_RE.search(xpath)
    if not m:
        return None, None
    if m.group(1) is not None:
        return m.group(1), "contains"
    if m.group(2) is not None:
        return m.group(2), "exact"
    if m.group(3) is not None:
        return m.group(3), "exact"
    if m.group(4) is not None:
        return m.group(4), "contains"
    return None, None


def _find_invisible_char_suffix(page_text: str | None, target: str) -> Optional[dict]:
    """Detect ``target`` immediately followed by invisible/zero-width
    character(s) in the raw pageText capture.

    pageText is a plain-text innerText snapshot, captured independently of
    page-source.html, so it can hold content (e.g. inside a CDK overlay/portal)
    that never makes it into the saved HTML. Finding the label there — with a
    telltale invisible codepoint stuck to it — is a mechanical, reproducible
    signal that the element is fine and only the exact-equality text
    comparison is broken.
    """
    if not page_text or not target:
        return None
    pattern = re.compile(re.escape(target), re.IGNORECASE)
    for m in pattern.finditer(page_text):
        j = m.end()
        found: list[str] = []
        while j < len(page_text) and page_text[j] in _INVISIBLE_CHARS:
            found.append(page_text[j])
            j += 1
        if found:
            return {
                "matched_text": page_text[m.start():j],
                "codepoints": ", ".join(f"U+{ord(c):04X}" for c in found),
            }
    return None


def extract_selector(call_log: str | None) -> dict[str, Any] | None:
    """Extract the failed selector and its type from the Playwright call log.

    Returns ``{"raw": str, "type": str, "value": str, ...}`` or None.
    """
    if not call_log:
        return None

    lines = [ln.strip() for ln in call_log.splitlines() if ln.strip()]
    for line in lines:
        # getByText
        m = _GETBY_TEXT_RE.search(line)
        if m:
            return {"raw": line, "type": "text", "value": m.group(1)}
        # getByRole
        m = _GETBY_ROLE_RE.search(line)
        if m:
            return {"raw": line, "type": "role", "role": m.group(1), "name": m.group(2)}
        # getByLabel
        m = _GETBY_LABEL_RE.search(line)
        if m:
            return {"raw": line, "type": "label", "value": m.group(1)}
        # getByPlaceholder
        m = _GETBY_PLACEHOLDER_RE.search(line)
        if m:
            return {"raw": line, "type": "placeholder", "value": m.group(1)}
        # locator('...') or waiting for selector "..."
        # Use findall and take the LAST match — chained calls like
        # locator("A").locator("B") produce two matches; B is the failing one.
        all_matches = list(_LOCATOR_RE.finditer(line))
        if all_matches:
            m = all_matches[-1]
            selector = m.group(2) or m.group(4)
            if not selector:
                continue
            # text= prefix
            tm = _TEXT_PREFIX_RE.match(selector)
            if tm:
                return {"raw": line, "type": "text", "value": tm.group(1).strip("'\" ")}
            # XPath
            if selector.startswith("//") or selector.startswith("/"):
                return {"raw": line, "type": "xpath", "value": selector}
            # Treat remaining as CSS
            return {"raw": line, "type": "css", "value": selector}

    return None


# ---------------------------------------------------------------------------
# DOM matching
# ---------------------------------------------------------------------------

def _parse_html(html_path: str) -> etree._Element | None:
    try:
        content = Path(html_path).read_text(encoding="utf-8", errors="replace")
        return lxml_html.fromstring(content)
    except Exception:
        return None


def _match_xpath(tree: etree._Element, xpath: str) -> list[etree._Element]:
    try:
        results = tree.xpath(xpath)
        if isinstance(results, list):
            return [r for r in results if isinstance(r, etree._Element)]
        return []
    except Exception:
        return []


def _match_css(tree: etree._Element, css: str) -> list[etree._Element]:
    try:
        from lxml.cssselect import CSSSelector
        sel = CSSSelector(css)
        return sel(tree)
    except Exception:
        return []


def _match_text(tree: etree._Element, text: str) -> list[etree._Element]:
    """Find elements whose text content contains the target text."""
    text_lower = text.lower()
    matches = []
    for el in tree.iter():
        el_text = (el.text or "") + (el.tail or "")
        full = el_text + " " + " ".join(
            (c.text or "") + (c.tail or "") for c in el
        )
        if text_lower in full.lower():
            matches.append(el)
    return matches


def _match_role(tree: etree._Element, role: str, name: str | None) -> list[etree._Element]:
    """Match elements by ARIA role (or implicit HTML role) and optional accessible name."""
    role_map = {
        "button": ["button", "input[@type='submit']", "input[@type='button']", "*[@role='button']"],
        "link": ["a", "*[@role='link']"],
        "textbox": ["input[not(@type) or @type='text' or @type='email' or @type='password' or @type='search' or @type='tel' or @type='url']", "textarea", "*[@role='textbox']"],
        "checkbox": ["input[@type='checkbox']", "*[@role='checkbox']"],
        "radio": ["input[@type='radio']", "*[@role='radio']"],
        "combobox": ["select", "*[@role='combobox']", "*[@role='listbox']"],
        "heading": ["h1", "h2", "h3", "h4", "h5", "h6", "*[@role='heading']"],
        "dialog": ["dialog", "*[@role='dialog']", "*[@role='alertdialog']"],
        "tab": ["*[@role='tab']"],
        "row": ["tr", "*[@role='row']"],
        "cell": ["td", "th", "*[@role='cell']", "*[@role='gridcell']"],
        "menu": ["*[@role='menu']"],
        "menuitem": ["*[@role='menuitem']"],
        "option": ["option", "*[@role='option']"],
    }
    xpaths = role_map.get(role.lower(), [f"*[@role='{role}']"])
    seen_ids: set[int] = set()
    candidates = []
    for xp in xpaths:
        for el in _match_xpath(tree, f"//{xp}"):
            el_id = id(el)
            if el_id not in seen_ids:
                seen_ids.add(el_id)
                candidates.append(el)

    if not name:
        return candidates

    name_lower = name.lower()
    filtered = []
    for el in candidates:
        # Check aria-label, title, text content
        aria = (el.get("aria-label") or "").lower()
        title = (el.get("title") or "").lower()
        value = (el.get("value") or "").lower()
        text = (el.text_content() or "").strip().lower()
        if name_lower in aria or name_lower in title or name_lower in text or name_lower in value:
            filtered.append(el)
    return filtered


def _match_label(tree: etree._Element, label_text: str) -> list[etree._Element]:
    """Match form elements by their associated label text."""
    label_lower = label_text.lower()
    results = []
    for label in tree.iter("label"):
        text = (label.text_content() or "").strip().lower()
        if label_lower in text:
            for_id = label.get("for")
            if for_id:
                by_id = _match_xpath(tree, f"//*[@id='{for_id}']")
                results.extend(by_id)
            else:
                for child in label.iter():
                    if child.tag in ("input", "select", "textarea"):
                        results.append(child)
    # Also check aria-label
    results.extend(_match_xpath(tree, f"//*[@aria-label='{label_text}']"))
    return results


def _match_placeholder(tree: etree._Element, placeholder: str) -> list[etree._Element]:
    ph_lower = placeholder.lower()
    return [
        el for el in tree.iter()
        if (el.get("placeholder") or "").lower() == ph_lower
    ]


def _element_summary(el: etree._Element) -> str:
    tag = el.tag
    attrs = []
    for attr in ("id", "class", "role", "aria-label", "name", "type", "href"):
        val = el.get(attr)
        if val:
            attrs.append(f'{attr}="{val[:50]}"')
    text = (el.text_content() or "").strip()[:60]
    attr_str = " " + " ".join(attrs) if attrs else ""
    text_str = f" text={text!r}" if text else ""
    return f"<{tag}{attr_str}>{text_str}"


_HIDDEN_CSS_CLASSES: frozenset[str] = frozenset({
    "ng-hide", "d-none", "hidden", "invisible", "v-hide", "is-hidden",
    "hide", "ng-hidden", "v-show-false", "collapsed",
})


def _is_visible(el: etree._Element) -> bool:
    """Heuristic check for visibility — inline styles, HTML attributes, and common hide classes."""
    style = (el.get("style") or "").lower()
    if "display: none" in style or "display:none" in style:
        return False
    if "visibility: hidden" in style or "visibility:hidden" in style:
        return False
    hidden = el.get("hidden")
    if hidden is not None:
        return False
    aria_hidden = (el.get("aria-hidden") or "").lower()
    if aria_hidden == "true":
        return False
    cls = set((el.get("class") or "").split())
    if cls & _HIDDEN_CSS_CLASSES:
        return False
    return True


def _is_disabled(el: etree._Element) -> bool:
    if el.get("disabled") is not None:
        return True
    aria_disabled = (el.get("aria-disabled") or "").lower()
    return aria_disabled == "true"


# ---------------------------------------------------------------------------
# Text-elsewhere detection
# ---------------------------------------------------------------------------

def _find_text_elsewhere(tree: etree._Element, text: str) -> list[str]:
    """Search the full DOM for occurrences of the target text, returning
    element summaries of where it appears.  Capped at 5 results."""
    if not text or len(text) < 2:
        return []
    text_lower = text.lower()
    found = []
    for el in tree.iter():
        if not isinstance(el.tag, str):
            continue  # skip comments/PIs, whose .tag isn't a string
        if el.tag in ("script", "style", "noscript"):
            continue
        content = (el.text_content() or "").strip()
        # Also check value, placeholder, title, alt, aria-label attributes
        attr_text = " ".join(
            (el.get(a) or "") for a in ("value", "placeholder", "title", "alt", "aria-label")
        )
        combined = content + " " + attr_text
        if text_lower in combined.lower() and len(content) < 500:
            found.append(_element_summary(el))
            if len(found) >= 5:
                break
    return found


# ---------------------------------------------------------------------------
# Alternative selector suggestions
# ---------------------------------------------------------------------------

def _suggest_alternatives(
    tree: etree._Element, selector_info: dict, original_matches: list,
) -> list[str]:
    """Suggest alternative selectors when the original failed (0 matches)."""
    suggestions = []
    sel_type = selector_info.get("type")
    text_value = selector_info.get("name")
    if not text_value and sel_type == "xpath":
        xm = _XPATH_TEXT_RE.search(selector_info.get("value", ""))
        text_value = (xm.group(1) or xm.group(2) or xm.group(3) or xm.group(4)) if xm else None
    if not text_value and sel_type in ("text", "label", "placeholder"):
        text_value = selector_info.get("value")

    if not text_value:
        return suggestions

    # Try text match
    if sel_type != "text":
        text_matches = _match_text(tree, text_value)
        if text_matches:
            for m in text_matches[:3]:
                suggestions.append(f"text={text_value!r} matched {_element_summary(m)}")

    # Try role match if we know the role
    if sel_type == "xpath" and text_value:
        for role in ("button", "link", "textbox", "combobox"):
            role_matches = _match_role(tree, role, text_value)
            if role_matches:
                suggestions.append(
                    f"getByRole('{role}', {{name: '{text_value}'}}) matched "
                    f"{len(role_matches)} element(s): {_element_summary(role_matches[0])}"
                )
                break

    return suggestions[:3]




# ---------------------------------------------------------------------------
# Shadow DOM comment content search
# ---------------------------------------------------------------------------

def _find_text_in_shadow_content(html_path: str, text: str) -> Optional[dict]:
    """Search raw HTML for target text inside serialized shadow-root comment blocks.

    The Hooks.java shadow serializer inlines shadow root content as:
        <!--[shadow-root]-->...<slot>Some Text</slot>...<!--[/shadow-root]-->
    lxml treats these as opaque comments, so _find_text_elsewhere() misses them.
    This function searches the raw file bytes and, when the text is found inside
    a shadow block, also extracts the aria-label from the inner <button> to
    suggest a concrete replacement selector.

    Returns a dict with host_tag, aria_label, and suggested_selector, or None.
    """
    try:
        raw = Path(html_path).read_text(encoding="utf-8", errors="replace")
    except Exception:
        return None

    text_lower = text.lower()
    import re as _re
    blocks = _re.findall(
        r'<!--\[shadow-root\]-->(.*?)<!--\[/shadow-root\]-->',
        raw, _re.DOTALL,
    )
    for block in blocks:
        if text_lower not in block.lower():
            continue
        # Found. Try to extract aria-label from inner <button>.
        aria_match = _re.search(
            r'<button[^>]*aria-label=["\'](.*?)["\']',
            block, _re.IGNORECASE,
        )
        aria_label = aria_match.group(1) if aria_match else text
        # Find the host element tag that precedes this shadow block.
        block_pos = raw.find(block)
        prefix = raw[max(0, block_pos - 500):block_pos]
        host_match = _re.search(r'<([a-z][a-z0-9-]*)(?:[^>]*)>\s*$', prefix, _re.IGNORECASE)
        host_tag = host_match.group(1) if host_match else "ds-toggle"
        return {
            "host_tag": host_tag,
            "aria_label": aria_label,
            "suggested_selector": f'{host_tag} button[aria-label="{aria_label}"]',
        }
    return None

# ---------------------------------------------------------------------------
# Fast-text helper
# ---------------------------------------------------------------------------

def _fast_text_from_selector(sel: dict[str, Any]) -> str | None:
    """Extract the human-readable text target from a parsed selector dict.

    Used for the pageText fast-path check: if the text isn't in the captured
    page text corpus, the element was definitely absent and we can skip the
    full lxml parse.
    """
    stype = sel.get("type")
    if stype == "xpath":
        xm = _XPATH_TEXT_RE.search(sel.get("value", ""))
        return (xm.group(1) or xm.group(2) or xm.group(3) or xm.group(4)) if xm else None
    if stype in ("text", "label", "placeholder"):
        return sel.get("value")
    if stype == "role":
        return sel.get("name")
    return None


# ---------------------------------------------------------------------------
# Main analysis entry point
# ---------------------------------------------------------------------------

def analyze_offline(failure: dict[str, Any]) -> dict[str, Any] | None:
    """Analyze a failure using its saved page-source.html.

    Returns a compact receipt dict or None if analysis isn't possible.
    """
    artifacts = failure.get("failureArtifacts")
    if not artifacts:
        return None

    page_source_path = artifacts.get("pageSource")
    if not page_source_path or not Path(page_source_path).exists():
        return None

    call_log = failure.get("playwrightCallLog")
    error_msg = failure.get("errorMessage") or ""
    expected = failure.get("expectedValue")
    actual = failure.get("actualValue")

    # Extract selector early so the fast-path check can use it before HTML parse.
    selector_info = extract_selector(call_log)

    # --- Suggestion 3: pageText fast path ---
    # If the selector has a text target and pageText is available, check text
    # presence BEFORE paying the lxml parse cost.  Text absent from pageText
    # means the element was definitely not in the DOM — skip the full parse.
    # Skip fast path for XPaths with a class predicate: the class might exist
    # in the DOM even when the text label is gone, and we need the full parse
    # to catch that case (locator_ancestor_stale detection).
    page_text = (failure.get("pageText") or "").lower()
    if selector_info and page_text:
        fast_text = _fast_text_from_selector(selector_info)
        sel_val = selector_info.get("value", "")
        has_class_pred = (
            selector_info.get("type") == "xpath"
            and bool(_XPATH_CLASS_RE.search(sel_val))
        )
        if fast_text and fast_text.lower() not in page_text and not has_class_pred:
            # pageText is a lightweight, separately-captured text corpus that —
            # like XPath text() predicates — cannot see inside an open shadow
            # root. "Absent from pageText" is therefore not proof the element
            # is absent from the DOM; check the shadow-serialized HTML before
            # giving up, since that's exactly where design-system components
            # (ds-button, etc.) put their visible label text.
            shadow_hit = _find_text_in_shadow_content(page_source_path, fast_text)
            if shadow_hit:
                return {
                    "analysisTier": "tier2_offline_dom",
                    "verdict": "script_issue_fix_proposed",
                    "confidence": "high",
                    "diagnosisKind": "shadow_dom_locator",
                    "evidence": [
                        f"Selector type: {selector_info['type']}",
                        f"Locator text {fast_text!r} absent from the lightweight pageText capture, but found "
                        f"inside serialized shadow DOM (<!--[shadow-root]--> block) — pageText/XPath cannot "
                        f"see this. Host element: <{shadow_hit['host_tag']}>. "
                        f"Suggested selector: {shadow_hit['suggested_selector']!r}",
                    ],
                    "recommendedAction": (
                        "Replace the XPath text selector with a CSS selector targeting the aria-label "
                        "attribute on the interactive element inside the shadow root. Suggested: "
                        f"{shadow_hit['suggested_selector']!r}. Playwright CSS selectors pierce open "
                        "Shadow DOM; XPath does not."
                    ),
                    "artifacts": {
                        "pageSource": page_source_path,
                        "screenshot": artifacts.get("screenshot"),
                    },
                }
            return {
                "analysisTier": "tier2_offline_dom",
                "verdict": "needs_investigation",
                "confidence": "medium",
                "diagnosisKind": "element_absent",
                "evidence": [
                    f"Selector type: {selector_info['type']}",
                    f"Locator text {fast_text!r} is absent from the captured page-text snapshot — "
                    "element was not in the DOM at failure time.",
                    "Cannot distinguish product regression from wrong page state without live replay.",
                ],
                "recommendedAction": (
                    "Needs live replay — element absent from page snapshot; "
                    "cause unclear without re-running the scenario."
                ),
                "artifacts": {
                    "pageSource": page_source_path,
                    "screenshot": artifacts.get("screenshot"),
                },
            }

    tree = _parse_html(page_source_path)
    if tree is None:
        return None

    evidence: list[str] = []
    verdict = "needs_investigation"
    confidence = "low"
    diagnosisKind = "element_absent"

    if selector_info:
        sel_type = selector_info["type"]
        matches: list[etree._Element] = []

        if sel_type == "xpath":
            matches = _match_xpath(tree, selector_info["value"])
        elif sel_type == "css":
            matches = _match_css(tree, selector_info["value"])
        elif sel_type == "text":
            matches = _match_text(tree, selector_info["value"])
        elif sel_type == "role":
            matches = _match_role(tree, selector_info["role"], selector_info.get("name"))
        elif sel_type == "label":
            matches = _match_label(tree, selector_info["value"])
        elif sel_type == "placeholder":
            matches = _match_placeholder(tree, selector_info["value"])

        evidence.append(f"Selector type: {sel_type}")
        evidence.append(f"Failed selector matched {len(matches)} node(s) in failure DOM.")

        if len(matches) == 0:
            # Selector found nothing — likely stale
            evidence.append("Selector is stale or target element is absent from the page.")

            # Search for the text elsewhere — extract meaningful text from selector
            text_target = selector_info.get("name")
            if not text_target and sel_type == "xpath":
                xm = _XPATH_TEXT_RE.search(selector_info.get("value", ""))
                text_target = (xm.group(1) or xm.group(2) or xm.group(3) or xm.group(4)) if xm else None
            if not text_target and sel_type in ("text", "label", "placeholder"):
                text_target = selector_info.get("value")
            if text_target:
                elsewhere = _find_text_elsewhere(tree, text_target)
                if elsewhere:
                    evidence.append(
                        f"Text {text_target!r} found elsewhere in DOM: {'; '.join(elsewhere[:3])}"
                    )
                    verdict = "script_issue_fix_proposed"
                    confidence = "medium"
                else:
                    evidence.append(f"Text {text_target!r} not found anywhere in the failure DOM.")

                    # Invisible/zero-width character check: only exact-match XPath
                    # predicates (text()='X', normalize-space(...)='X') are vulnerable —
                    # contains()-style predicates still match "X" as a substring.
                    predicate_kind = None
                    if sel_type == "xpath":
                        _, predicate_kind = _extract_xpath_text_and_kind(selector_info.get("value", ""))
                    if predicate_kind == "exact":
                        invisible_hit = _find_invisible_char_suffix(failure.get("pageText"), text_target)
                        if invisible_hit:
                            evidence.append(
                                f"Text {text_target!r} IS present in the captured pageText as "
                                f"{invisible_hit['matched_text']!r} — immediately followed by "
                                f"invisible character(s) {invisible_hit['codepoints']}. "
                                f"normalize-space() does not strip these codepoints, so the "
                                f"exact-match predicate never equals the literal target even "
                                f"though the label is visibly correct on screen."
                            )
                            verdict = "script_issue_fix_proposed"
                            confidence = "high"
                            diagnosisKind = "invisible_char_text_mismatch"

                    # Partial match: detect button/link text that was renamed.
                    # e.g. target="Save List" but DOM now has <button>Save</button>.
                    # Require both strings >= 4 chars and one contains the other.
                    if diagnosisKind == "element_absent" and len(text_target) >= 4:
                        t_lower = text_target.lower()
                        for el in tree.iter("button", "a", "input"):
                            el_text = (el.text_content() or "").strip()
                            el_lower = el_text.lower() if el_text else ""
                            if len(el_lower) < 4:
                                continue
                            if el_lower in t_lower or t_lower in el_lower:
                                evidence.append(
                                    f"Partial text match: target {text_target!r} overlaps with "
                                    f"element text {el_text!r} at {_element_summary(el)} "
                                    f"— element may have been renamed."
                                )
                                verdict = "script_issue_fix_proposed"
                                confidence = "medium"
                                diagnosisKind = "text_renamed"
                                break

            # Shadow DOM: text not found by lxml — search raw HTML comment blocks.
            # The recursive shadow serializer in Hooks.java inlines shadow root
            # content as <!--[shadow-root]-->...<!--[/shadow-root]-->, which lxml
            # ignores. A positive hit here is a definitive script issue.
            if text_target and diagnosisKind not in ("shadow_dom_locator", "invisible_char_text_mismatch"):
                shadow_hit = _find_text_in_shadow_content(page_source_path, text_target)
                if shadow_hit:
                    evidence.append(
                        f"Text {text_target!r} found inside serialized shadow DOM "
                        f"(<!--[shadow-root]--> block) — lxml cannot see this. "
                        f"XPath selectors cannot pierce Shadow DOM. "
                        f"Host element: <{shadow_hit['host_tag']}>. "
                        f"Suggested selector: {shadow_hit['suggested_selector']!r}"
                    )
                    verdict = "script_issue_fix_proposed"
                    confidence = "high"
                    diagnosisKind = "shadow_dom_locator"

            # For XPath with contains(@class,'X'): search DOM broadly for that class.
            # If found → full locator is too narrow / wrong ancestor → script issue.
            # If not found → element absent from snapshot (still escalates).
            # A locator can carry multiple class predicates (e.g. an ancestor
            # class AND a target class); checking only the first one misses a
            # broad match on a later predicate that would have proven the
            # target element is still present under a different ancestor.
            if verdict == "needs_investigation" and sel_type == "xpath":
                class_names = _XPATH_CLASS_RE.findall(selector_info.get("value", ""))
                if class_names:
                    found_any = False
                    for class_name in class_names:
                        broad = _match_xpath(tree, f"//*[contains(@class,'{class_name}')]")
                        if broad:
                            found_any = True
                            evidence.append(
                                f"Class '{class_name}' found on {len(broad)} element(s) in DOM "
                                f"but full locator matched 0 — locator ancestor/structure is stale. "
                                f"First: {_element_summary(broad[0])}"
                            )
                            verdict = "script_issue_fix_proposed"
                            confidence = "medium"
                            diagnosisKind = "locator_ancestor_stale"
                    if not found_any:
                        # Last resort: a kebab-case class token (e.g. "hide-all") often
                        # mirrors the element's visible label 1:1 after a design-system
                        # migration replaces raw CSS classes with a labeled wrapper
                        # component (e.g. <app-ds-button-wrapper label="Hide All">).
                        # The class itself is gone, but the label text usually survives
                        # — try it before giving up, including inside shadow content.
                        guessed = False
                        for class_name in class_names:
                            if not re.fullmatch(r"[a-z0-9]+(?:-[a-z0-9]+)+", class_name):
                                continue
                            guess_label = " ".join(w.capitalize() for w in class_name.split("-"))
                            shadow_hit = _find_text_in_shadow_content(page_source_path, guess_label)
                            if shadow_hit:
                                evidence.append(
                                    f"Class token '{class_name}' no longer exists in the DOM, but maps to "
                                    f"a plausible visible label {guess_label!r}, found inside serialized "
                                    f"shadow DOM (<!--[shadow-root]--> block). Host element: "
                                    f"<{shadow_hit['host_tag']}>. Suggested selector: "
                                    f"{shadow_hit['suggested_selector']!r}"
                                )
                                verdict = "script_issue_fix_proposed"
                                confidence = "medium"
                                diagnosisKind = "shadow_dom_locator"
                                guessed = True
                                break
                            text_hits = _find_text_elsewhere(tree, guess_label)
                            if text_hits:
                                evidence.append(
                                    f"Class token '{class_name}' no longer exists in the DOM, but maps to "
                                    f"a plausible visible label {guess_label!r}, found elsewhere in DOM: "
                                    f"{'; '.join(text_hits[:2])}"
                                )
                                verdict = "script_issue_fix_proposed"
                                confidence = "medium"
                                diagnosisKind = "locator_ancestor_stale"
                                guessed = True
                                break
                        if not guessed:
                            evidence.append(
                                f"None of the classes {class_names} found anywhere in snapshot — "
                                f"element may be absent from this page state."
                            )

            alternatives = _suggest_alternatives(tree, selector_info, matches)
            if alternatives:
                evidence.append("Alternative selectors: " + "; ".join(alternatives))
                verdict = "script_issue_fix_proposed"
                # Don't overwrite a high-confidence shadow/invisible-char diagnosis already set above.
                if diagnosisKind not in ("shadow_dom_locator", "invisible_char_text_mismatch"):
                    confidence = "medium"
                # Don't overwrite a more specific diagnosisKind already set above.
                if diagnosisKind == "element_absent":
                    diagnosisKind = "element_absent"

        elif len(matches) == 1:
            el = matches[0]
            visible = _is_visible(el)
            disabled = _is_disabled(el)
            evidence.append(f"Matched element: {_element_summary(el)}")

            if not visible:
                evidence.append(
                    "Matched element is HIDDEN "
                    "(display:none / visibility:hidden / aria-hidden / CSS hide class)."
                )
                verdict = "needs_investigation"
                confidence = "medium"
                diagnosisKind = "element_hidden"
            elif disabled:
                evidence.append("Matched element is DISABLED.")
                verdict = "needs_investigation"
                confidence = "medium"
                diagnosisKind = "element_disabled"
            else:
                # Element present, visible, enabled — the Playwright timeout was a
                # timing race, not a missing element. Resolve as intermittent.
                evidence.append(
                    "Matched element is visible and enabled in snapshot — "
                    "Playwright timeout was a timing race, not a missing element."
                )
                verdict = "not_reproduced_intermittent"
                confidence = "medium"
                diagnosisKind = "timing_race"

        else:
            # Multiple matches: most likely selector is too broad (script issue), but a
            # product bug rendering duplicate elements cannot be ruled out from the snapshot.
            evidence.append(
                f"Multiple matches ({len(matches)}): selector is ambiguous. "
                f"Likely too broad; product rendering duplicate elements cannot be ruled out. "
                f"First: {_element_summary(matches[0])}"
            )
            verdict = "script_issue_fix_proposed"
            confidence = "medium"
            diagnosisKind = "ambiguous_selector"

    # Assertion analysis: check expected/actual in DOM
    if expected and actual:
        expected_in_dom = _find_text_elsewhere(tree, expected)
        actual_in_dom = _find_text_elsewhere(tree, actual)
        if actual_in_dom and not expected_in_dom:
            evidence.append(
                f"Actual value {actual!r} present in DOM; expected {expected!r} absent — "
                f"app shows different value than test expects."
            )
        elif expected_in_dom and actual_in_dom:
            evidence.append(f"Both expected {expected!r} and actual {actual!r} present in DOM.")
        elif not actual_in_dom and not expected_in_dom:
            evidence.append(
                f"Neither expected {expected!r} nor actual {actual!r} found in DOM — "
                f"values may be in a non-rendered attribute or loaded dynamically."
            )

    # Check for error/status messages in the DOM
    error_indicators = _match_xpath(tree, "//*[contains(@class, 'error') or contains(@class, 'alert')]")
    visible_errors = [e for e in error_indicators if _is_visible(e) and (e.text_content() or "").strip()]
    if visible_errors:
        error_texts = [
            (el.text_content() or "").strip()[:100] for el in visible_errors[:3]
        ]
        evidence.append(f"Visible error/alert elements in DOM: {error_texts}")

    if not evidence:
        return None

    # Build a specific recommendedAction keyed to diagnosisKind.
    _action_map: dict[str, str] = {
        "text_renamed": (
            "Update the locator text in the page object to match the renamed element."
        ),
        "locator_ancestor_stale": (
            "Update the XPath ancestor chain in the page object — "
            "the class exists in the DOM but the surrounding structure changed."
        ),
        "ambiguous_selector": (
            "Narrow the locator in the page object so it matches exactly one element."
        ),
        "element_hidden": (
            "Investigate why the matched element is hidden at the time of the test — "
            "may need an earlier wait or a different page state."
        ),
        "element_disabled": (
            "Investigate why the matched element is disabled — "
            "check whether a required prior action (save, submit) did not complete."
        ),
        "timing_race": (
            "No locator fix needed — element was present and visible in the DOM snapshot; "
            "add an explicit wait before the action to handle the timing race."
        ),
        "element_absent": (
            "Needs live replay — element absent from page snapshot; "
            "cause unclear without re-running the scenario."
        ),
        "shadow_dom_locator": (
            "Replace the XPath text selector with a CSS selector targeting the "
            "aria-label attribute on the interactive element inside the shadow root. "
            "Use the suggested_selector from evidence (e.g. "
            "`ds-toggle button[aria-label=\'X\']`). "
            "Playwright CSS selectors pierce open Shadow DOM; XPath does not."
        ),
        "invisible_char_text_mismatch": (
            "Do not treat this as a missing/renamed element — the label is correct, only "
            "the exact-match text comparison is broken. Strip the invisible codepoint(s) "
            "before comparing, e.g. translate(normalize-space(text()), '<char>', '') = 'X', "
            "or switch to a contains()/attribute-based locator that does not require exact "
            "equality."
        ),
    }
    recommended_action = _action_map.get(diagnosisKind, "Needs live replay to determine root cause.")
    # For element_absent with alternative selectors found, override to be more actionable.
    if diagnosisKind == "element_absent" and any("Alternative selectors" in e for e in evidence):
        recommended_action = "Replace the stale locator with one of the suggested alternatives in the page object."

    return {
        "analysisTier": "tier2_offline_dom",
        "verdict": verdict,
        "confidence": confidence,
        "diagnosisKind": diagnosisKind,
        "evidence": evidence,
        "recommendedAction": recommended_action,
        "artifacts": {
            "pageSource": page_source_path,
            "screenshot": artifacts.get("screenshot"),
        },
    }
