# ElevanceAmazonAutomation

Java 21 + Selenium WebDriver + Maven + TestNG automation of six internship tasks on **Amazon.in**, run in **Microsoft Edge**.
All page data (titles, prices, ratings, profile name, cart totals) is read from the live browser - nothing is invented.

## 1. The six tasks (all times in IST, `Asia/Kolkata`, windows are `[start, end)`)

| # | Class | Requirement | Window |
|---|-------|-------------|--------|
| 1 | `Test1` | Search a configurable product; pick a **non-electronic** product whose title does **not begin with A, B, C or D**; open it and validate page, title and price | 15:00 - 18:00 |
| 2 | `Test2` | Search; apply/validate filters: brand starts with **C**, price **> ₹2,000**, rating **> 4.0** | 15:00 - 18:00 |
| 3 | `Test3` | Search, add to cart, payment + order confirmation, amount **> ₹500** (live Amazon stops safely; confirmation is shown on a labelled **mock** checkout) | 18:00 - 19:00 |
| 4 | `Test4` | Login, check profile details, profile name must **not contain A, C, G, I, L, K** (case-insensitive) | 12:00 - 15:00 |
| 5 | `Test5` | Add multiple products; real cart total **> ₹2,000**; username matches `^[A-Za-z0-9]{10}$` | 18:00 - 19:00 |
| 6 | `Test6` + `PriceMonitor` | Monitor a product price; e-mail when it drops below a threshold | none |

The time is checked **before** the browser opens. Outside the window the test is reported **SKIPPED** ("Not executed: ..."), never passed.

## 2. Tech stack
Java 21, Selenium 4.27.0 (Selenium Manager fetches `msedgedriver`), TestNG 7.10.2, Maven, Jakarta Mail 2.1.x (Eclipse Angus), Microsoft Edge.

## 3. Folder structure
```
ElevanceAmazonAutomation/
  pom.xml  testng.xml  config.properties  README.md  .gitignore
  src/main/java/elevanceskills/intern/
    BasePage, AmazonHomePage, SearchResultsPage, ProductPage, CartPage, LoginPage, AccountPage
    BlockedException, MonitorSettings, PriceMonitor        <- Task 6 entry point
    utils/ TimeValidator, PriceUtil, ValidationUtil, ConfigUtil, ScreenshotUtil, EmailUtil,
           BrowserFactory, PriceAlertTracker
  src/test/java/elevanceskills/intern/
    BaseTest, Test1 ... Test6
    *UnitTest (TimeValidator, ValidationUtil, PriceUtil, PriceAlertTracker, SmtpSettings, MonitorSettings)
  src/test/resources/mock-checkout/checkout.html          <- clearly labelled MOCK checkout page
```
Note: `BaseTest` lives in `src/test/java` (not `src/main`) so TestNG is a test-only dependency.

## 4. Prerequisites
- JDK 21 (`java -version` must say 21) and Maven 3.9+ (`mvn -v`, must report Java 21)
- Microsoft Edge (current version) and internet access (Selenium Manager downloads the matching driver)
- Eclipse IDE for Java Developers (includes m2e)

## 5. Import into Eclipse
1. **File > Import > Maven > Existing Maven Projects** > browse to the `ElevanceAmazonAutomation` folder > Finish.
2. Right-click the project > **Maven > Update Project...** > tick *Force Update of Snapshots/Releases* > OK.
3. Project > Properties > **Java Compiler**: compliance level **21**. Window > Preferences > Java > **Installed JREs**: a JDK 21.
4. Optional: install the *TestNG for Eclipse* plugin to right-click `testng.xml` > Run As > TestNG Suite. The Maven commands below always work.

## 6. Configuration
- Edit `config.properties` (every key is documented in the file). Override any key on the command line: `-Dtask1.searchKeyword="steel bottle"`.
- Optional git-ignored `config.local.properties` overrides `config.properties`.
- **Secrets only in environment variables** (never in files):

| Variable | Used by |
|----------|---------|
| `AMAZON_EMAIL`, `AMAZON_PASSWORD` | Task 4 (optional; otherwise log in manually) |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_FROM`, `SMTP_TO` | Task 6 monitor |

Windows (cmd): `set AMAZON_EMAIL=you@example.com` - PowerShell: `$env:AMAZON_EMAIL="you@example.com"` - Linux/macOS: `export AMAZON_EMAIL=...`
Set them in the same terminal you run Maven from (in Eclipse: Run Configurations > Environment).

**Task 6 setup:** replace `task6.productUrl=https://www.amazon.in/dp/REPLACE_WITH_ASIN` with a real product URL.

## 7. Commands (run from the project root)

| What | Command |
|------|---------|
| Offline utility unit tests only (no browser, no internet) | `mvn test -Dtest="*UnitTest"` |
| Whole suite (`testng.xml`) | `mvn test` |
| One task, e.g. Task 1 | `mvn test -Dtest=Test1` (use `Test2` ... `Test6`) |
| Price monitor (Task 6) | `mvn compile exec:java` |
| Stop the monitor | `Ctrl+C`, or create the file `stop-monitor.flag` in the project root |

`-Dtest=...` bypasses the suite file. If your Maven still runs the full suite, use `-Dgroups=task1` (groups `task1`...`task6`, `unit`, `browser`).
The normal `mvn test` **never starts the monitoring loop**; Task 6 inside the suite only validates config and reads one price.

## 8. Time-window behaviour and prerequisites per task
- Run each task inside its window (IST). Check the time: PowerShell `[TimeZoneInfo]::ConvertTimeBySystemTimeZoneId((Get-Date),'India Standard Time')`.
- **Task 4** needs a real Amazon account. With `AMAZON_EMAIL/AMAZON_PASSWORD` the framework types them in; any CAPTCHA/OTP/passkey step is **left to you** in the open browser (up to `browser.manualInterventionSeconds`). If login is not finished, the test is **SKIPPED (BLOCKED)**. Its result is the real name Amazon shows: if that name contains A/C/G/I/L/K the test **fails** truthfully.
- **Tasks 1, 2, 3, 5** work without login (guest cart). Use `browser.headless=false` so you can solve a CAPTCHA if Amazon shows one.
- **Task 5** reads the whole cart; a fresh browser session has a fresh guest cart. If the total is not > ₹2,000, raise `task5.minItemPrice` or change `task5.searchKeywords`.
- Task 5 username test checks `task5.username` (a test input), not your Amazon identity.

## 9. Live-checkout safety limits (Task 3)
- The framework **never places a real order**, never enters card/bank details, and never clicks anything on checkout or payment pages.
- Live part: search > add to cart > verify the selected product and the real cart amount > click *Proceed to checkout* > **stop** at whatever Amazon shows (normally sign-in). The log states: *"live order confirmation was NOT performed"*.
- Mock part: pays and confirms on `mock-checkout/checkout.html`, a local page with a red **MOCK** banner. The confirmation id starts with `MOCK-`. It reuses the real cart amount but **is not an Amazon order**.

## 10. Reports, logs, screenshots
- TestNG/Surefire reports: `target/surefire-reports/` (`index.html`, `emailable-report.html`, `*-output.txt`)
- Failure/blocked screenshots: `target/screenshots/`
- Monitor log: console only. All of these are git-ignored.

## 11. Troubleshooting
- *Skipped: "Not executed: ... OUTSIDE the permitted window"* - run inside the window.
- *BLOCKED: CAPTCHA* - Amazon sometimes challenges automated browsers. It is not bypassed here: solve it by hand (headed mode) or try later.
- *Driver / Edge errors* - update Edge; check internet/proxy (Selenium Manager downloads the driver).
- *Locators stop matching* - Amazon changes its layout; the log shows what could not be read (nothing is faked).
- *`config.properties was not found`* - run Maven from the project root.
- *Wrong Java* - `mvn -v` must show Java 21; set `JAVA_HOME`.

## 12. GitHub upload
```
git init
git add .
git status            # review: no target/, no passwords, no config.local.properties
git commit -m "Elevance Skills internship: Amazon.in Selenium automation"
git branch -M main
git remote add origin https://github.com/<your-user>/ElevanceAmazonAutomation.git
git push -u origin main
```
Create the empty repository on github.com first. Verify public access by opening the repo URL in a private/incognito window (or logged out).

## 13. Final submission checklist
- [ ] `mvn test -Dtest="*UnitTest"` passes
- [ ] Each task run inside its IST window; results/screenshots saved
- [ ] `config.properties` has no passwords; Task 6 URL replaced and tested with your own SMTP variables
- [ ] `git status` shows no secrets, `target/` or browser data
- [ ] Repository pushed and opens in an incognito window
- [ ] Repository link submitted as Elevance Skills instructs (acceptance/stipend decisions are theirs)
