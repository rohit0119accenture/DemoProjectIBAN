# Phase 3 — UI test integration for bank-transfer-app
#
# Targets:
#   make up    Extend pom.xml, copy the E2E test into bank-transfer-app, run it.
#   make help  List available targets.
#
# Prerequisites for `make up`:
#   - Backend running on http://localhost:8089
#   - Frontend running on http://localhost:5173
#   - Chrome installed on the host (Selenium drives a headless instance)

BANK_APP    := ../bank-transfer-app
POM         := $(BANK_APP)/pom.xml
TEST_REL    := src/test/java/com/demobank/transfer/e2e/IbanValidationE2E.java
SRC_TEST    := $(TEST_REL)
DST_TEST    := $(BANK_APP)/$(TEST_REL)
DST_DIR     := $(dir $(DST_TEST))
TEST_CLASS  := IbanValidationE2E

.PHONY: up help pom-extend copy-test run-test

up: pom-extend copy-test run-test ## Run the full Phase 3 integration: pom + test copy + mvn test

help: ## Show available targets
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-12s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

pom-extend: ## Add selenium-java dependency + surefire *E2E include to pom (idempotent)
	@echo ">> pom-extend: $(POM)"
	@python3 -c "$$POM_EXTEND_PY" "$(POM)"

copy-test: ## Copy IbanValidationE2E.java into bank-transfer-app
	@echo ">> copy-test: $(SRC_TEST) -> $(DST_TEST)"
	@mkdir -p "$(DST_DIR)"
	@cp -p "$(SRC_TEST)" "$(DST_TEST)"

run-test: ## Run the E2E suite via Maven Surefire
	@echo ">> run-test: mvn test -Dtest=$(TEST_CLASS)"
	@cd "$(BANK_APP)" && mvn -q test -Dtest=$(TEST_CLASS)

# --- Inline Python helper for idempotent pom edits ------------------------
# Reads the pom, adds the selenium-java dependency if missing and the
# `**/*E2E.java` Surefire include if missing. Writes only on change.
define POM_EXTEND_PY
import pathlib, re, sys

pom_path = pathlib.Path(sys.argv[1])
src = pom_path.read_text()
original = src
notes = []

SELENIUM_VERSION = "4.27.0"

SELENIUM_DEP = """        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>4.27.0</version>
            <scope>test</scope>
        </dependency>
"""

SUREFIRE_PLUGIN = """            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <includes>
                        <include>**/*Test.java</include>
                        <include>**/*IT.java</include>
                        <include>**/*E2E.java</include>
                    </includes>
                </configuration>
            </plugin>
"""

# 1) selenium-java dependency
if "<artifactId>selenium-java</artifactId>" in src:
    notes.append("   = selenium-java dependency already present")
else:
    if "</dependencies>" not in src:
        sys.exit("   ! could not find </dependencies> in pom")
    src = src.replace("</dependencies>", SELENIUM_DEP + "    </dependencies>", 1)
    notes.append("   + selenium-java dependency injected")

# 1b) Override Spring Boot's BOM-managed selenium.version (2.7.x pins to 4.1.4,
#     which lacks Selenium Manager — required by ChromeDriver auto-resolution).
if re.search(r"<selenium\.version>" + re.escape(SELENIUM_VERSION) + r"</selenium\.version>", src):
    notes.append("   = selenium.version property already at " + SELENIUM_VERSION)
elif re.search(r"<selenium\.version>[^<]*</selenium\.version>", src):
    src = re.sub(
        r"<selenium\.version>[^<]*</selenium\.version>",
        "<selenium.version>" + SELENIUM_VERSION + "</selenium.version>",
        src,
        count=1,
    )
    notes.append("   ~ selenium.version property upgraded to " + SELENIUM_VERSION)
else:
    new, n = re.subn(
        r"(<properties>)(.*?)(</properties>)",
        lambda m: m.group(1) + m.group(2).rstrip()
                  + "\n        <selenium.version>" + SELENIUM_VERSION + "</selenium.version>\n    "
                  + m.group(3),
        src,
        count=1,
        flags=re.DOTALL,
    )
    if n == 0:
        notes.append("   ! no <properties> block found to inject selenium.version")
    else:
        src = new
        notes.append("   + selenium.version=" + SELENIUM_VERSION + " property added")

# 2) Surefire E2E include
if re.search(r"\*\*/\*E2E\.java", src):
    notes.append("   = surefire *E2E.java include already present")
else:
    if "<artifactId>maven-surefire-plugin</artifactId>" in src:
        # Add the include to the existing Surefire <includes>
        new, n = re.subn(
            r"(<artifactId>maven-surefire-plugin</artifactId>.*?<includes>)(.*?)(</includes>)",
            lambda m: m.group(1) + m.group(2).rstrip()
                      + "\n                        <include>**/*E2E.java</include>\n                    "
                      + m.group(3),
            src,
            count=1,
            flags=re.DOTALL,
        )
        if n == 0:
            # Surefire plugin exists but has no <includes> — leave a hint.
            notes.append("   ! maven-surefire-plugin found but no <includes> block — add manually (see pom-additions.xml)")
        else:
            src = new
            notes.append("   + surefire *E2E.java include added to existing plugin")
    else:
        # No Surefire plugin at all — add the full block.
        if "</plugins>" not in src:
            sys.exit("   ! could not find </plugins> in pom")
        src = src.replace("</plugins>", SUREFIRE_PLUGIN + "        </plugins>", 1)
        notes.append("   + maven-surefire-plugin added with *E2E.java include")

if src != original:
    pom_path.write_text(src)
    notes.append("   * wrote " + str(pom_path))
else:
    notes.append("   * no changes needed")

print("\n".join(notes))
endef
export POM_EXTEND_PY
