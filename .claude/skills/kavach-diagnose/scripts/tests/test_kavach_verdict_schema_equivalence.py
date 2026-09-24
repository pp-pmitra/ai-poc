"""Equivalence test between kavach-verdict.schema.json and validate_kavach_contract.py.

validate_kavach_contract.py is deliberately dependency-free (no jsonschema
package) and hand-reimplements the checks the JSON Schema describes. That
means the schema is never actually loaded or executed by any code — it is
documentation that can silently drift from what the validator really
enforces. This test doesn't add a jsonschema runtime dependency (the
dependency-free design is intentional); it just proves the two stay in
sync, by loading the schema's own enums/required-key lists and asserting
they equal the validator's VALID_*/*_REQUIRED_KEYS constants exactly. A
future edit to either file that isn't mirrored in the other fails here
immediately, instead of drifting unnoticed.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import validate_kavach_contract as contract

SCHEMA_PATH = Path(__file__).resolve().parents[4] / "contracts" / "kavach-verdict.schema.json"


def _load_schema() -> dict:
    return json.loads(SCHEMA_PATH.read_text(encoding="utf-8"))


def test_schema_file_exists_at_expected_path():
    assert SCHEMA_PATH.is_file(), f"expected schema at {SCHEMA_PATH}"


def test_doc_required_keys_match():
    schema = _load_schema()
    assert set(schema["required"]) == set(contract.DOC_REQUIRED_KEYS)


def test_group_required_keys_match():
    schema = _load_schema()
    group = schema["$defs"]["group"]
    assert set(group["required"]) == set(contract.GROUP_REQUIRED_KEYS)


def test_verdict_enum_matches():
    schema = _load_schema()
    group = schema["$defs"]["group"]
    schema_verdicts = set(group["properties"]["verdict"]["enum"])
    assert schema_verdicts == contract.VALID_VERDICTS


def test_analysis_tier_enum_matches():
    schema = _load_schema()
    group = schema["$defs"]["group"]
    schema_tiers = set(group["properties"]["analysisTier"]["enum"])
    assert schema_tiers == contract.VALID_TIERS


def test_confidence_enum_matches():
    schema = _load_schema()
    group = schema["$defs"]["group"]
    schema_confidence = set(group["properties"]["confidence"]["enum"])
    assert schema_confidence == contract.VALID_CONFIDENCE
