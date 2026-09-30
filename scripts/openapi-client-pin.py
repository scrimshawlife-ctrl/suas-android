#!/usr/bin/env python3
"""Handwritten SuasApi routes must match contract/client-routes.json.

Pass docs/openapi/v0.json to also require every non-local pin route to exist there.
Does not generate a client and does not add a second API version.
"""
import json
import pathlib
import re
import sys

root = pathlib.Path(__file__).resolve().parents[1]
pin = json.loads((root / "contract" / "client-routes.json").read_text())
source = (root / "app/src/main/java/com/example/suas/api/SuasApi.kt").read_text()


def norm(path: str) -> str:
    path = re.sub(r"\\\([^)]+\)", "{}", path)
    path = re.sub(r"\{[^}]+\}", "{}", path)
    return path


blocks = re.findall(
    r"@(GET|POST)\(\"([^\"]+)\"\)(.*?)(?=\n    @|\n\}|\Z)",
    source,
    re.S,
)
client = []
for method, path, body in blocks:
    client.append(
        {
            "method": method,
            "path": norm(path),
            "idempotency": "Idempotency-Key" in body,
        }
    )

expected = []
for route in pin["routes"]:
    expected.append(
        {
            "method": route["method"],
            "path": norm(route["path"]),
            "idempotency": bool(route["idempotency"]),
            "local_only": bool(route.get("local_only", False)),
            "raw": route["path"],
        }
    )

client_key = {(item["method"], item["path"], item["idempotency"]) for item in client}
pin_key = {(item["method"], item["path"], item["idempotency"]) for item in expected}
if client_key != pin_key:
    missing = sorted(pin_key - client_key)
    extra = sorted(client_key - pin_key)
    print("CLIENT PIN MISMATCH")
    print("missing from SuasApi:", missing)
    print("not in contract/client-routes.json:", extra)
    sys.exit(1)

if len(sys.argv) < 2:
    print("client routes match the pin")
    sys.exit(0)

openapi = json.loads(pathlib.Path(sys.argv[1]).read_text())
paths = openapi.get("paths", {})
errors = []
for route in pin["routes"]:
    if route.get("local_only"):
        continue
    operation = paths.get(route["path"], {}).get(route["method"].lower())
    if operation is None:
        errors.append(f"absent from OpenAPI: {route['method']} {route['path']}")
        continue
    has_key = "Idempotency" in json.dumps(operation)
    if bool(route["idempotency"]) != has_key:
        errors.append(
            f"idempotency mismatch: {route['method']} {route['path']} pin={route['idempotency']} openapi={has_key}"
        )
if errors:
    print("\n".join(errors))
    sys.exit(1)
print("client routes match the pin and OpenAPI")
