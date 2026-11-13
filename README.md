# ElevanceAmazonAutomation
### Amazon.in Web Automation Using Java, Selenium and TestNG

## Project Overview

This project was developed as part of my Elevance Skills internship. The objective was to automate six assigned tasks on Amazon.in using Java and Selenium WebDriver.

I used Maven to manage the project and TestNG to organize and execute the test cases. The automation reads information directly from the browser, including product titles, prices, ratings, profile details and shopping cart totals.

During development, I configured the project, integrated the automation classes, executed the test cases, analysed execution results and worked on resolving browser, configuration and SMTP-related issues.

## 1. Internship Tasks

The project covers the following six assigned tasks.

| Task | Automation class | Description |
|---|---|---|
| Task 1 | `Test1` | Search for and validate a suitable non-electronic product. |
| Task 2 | `Test2` | Search for products and verify brand, price and rating conditions. |
| Task 3 | `Test3` | Add a product to the cart, validate the amount and demonstrate a mock checkout. |
| Task 4 | `Test4` | Log in to Amazon and validate the profile name against specified character restrictions. |
| Task 5 | `Test5` | Add multiple products, verify the cart total and validate a ten-character username. |
| Task 6 | `Test6`, `PriceMonitor` | Retrieve product prices, monitor changes and send an email notification when the configured threshold is crossed. |

### Task execution time restrictions

Tasks 1–5 have specified execution windows in Indian Standard Time (IST).

| Task | Permitted time |
|---|---|
| Task 1 | 15:00–18:00 |
| Task 2 | 15:00–18:00 |
| Task 3 | 18:00–19:00 |
| Task 4 | 12:00–15:00 |
| Task 5 | 18:00–19:00 |
| Task 6 | No fixed time window |

The application checks the permitted time before opening the browser. A test executed outside its permitted window is reported as skipped rather than passed.

## 2. Technology Stack

- Java Development Kit 21
- Selenium WebDriver 4.27.0
- Maven
- TestNG 7.10.2
- Jakarta Mail
- Eclipse Angus Mail
- Google Chrome
- Eclipse IDE

Selenium Manager is used to manage the compatible ChromeDriver installation.

## 3. Project Structure

The project follows a Maven directory structure.

```text
ElevanceAmazonAutomation/
│
├── pom.xml
├── testng.xml
├── config.properties
├── README.md
├── .gitignore
│
└── src/
    ├── main/
    │   └── java/
    │       └── elevanceskills/intern/
    │           ├── BasePage.java
    │           ├── AmazonHomePage.java
    │           ├── SearchResultsPage.java
    │           ├── ProductPage.java
    │           ├── CartPage.java
    │           ├── LoginPage.java
    │           ├── AccountPage.java
    │           ├── BlockedException.java
    │           ├── MonitorSettings.java
    │           ├── PriceMonitor.java
    │           │
    │           └── utils/
    │               ├── BrowserFactory.java
    │               ├── ConfigUtil.java
    │               ├── EmailUtil.java
    │               ├── PriceUtil.java
    │               ├── PriceAlertTracker.java
    │               ├── ScreenshotUtil.java
    │               ├── TimeValidator.java
    │               └── ValidationUtil.java
    │
    └── test/
        ├── java/
        │   └── elevanceskills/intern/
        │       ├── BaseTest.java
        │       ├── Test1.java
        │       ├── Test2.java
        │       ├── Test3.java
        │       ├── Test4.java
        │       ├── Test5.java
        │       ├── Test6.java
        │       └── unit test classes
        │
        └── resources/
            └── mock-checkout/
                └── checkout.html
```

The exact package contents should match the source files included in the repository.

## 4. Implementation Details

### Task 1 – Product Selection

The automation searches Amazon.in using a configurable product keyword.

It checks the product title against the assigned restrictions, excludes electronic products and opens a qualifying product page for validation.

The product title and displayed price are obtained from the browser.

**Execution result:** The assigned product-selection test was successfully executed.

### Task 2 – Product Search and Filtering

The automation searches for running shoes and checks the following conditions:

- Brand begins with the letter C.
- Product price is greater than ₹2,000.
- Customer rating is greater than 4.0.

The conditions are checked using product information retrieved from Amazon.

**Execution result:** The search was performed, but no product satisfying all the required conditions was identified during the execution. The task's qualifying-product condition was therefore not achieved.

### Task 3 – Cart and Checkout Validation

The automation searches for a backpack, adds a suitable product to the cart and checks that its price exceeds ₹500.

For safety, the project does not place a real Amazon order or enter payment-card or banking information.

The live Amazon workflow is stopped before an actual payment or order is submitted. A separate local checkout page demonstrates the payment and confirmation flow using a clearly labelled mock transaction.

Mock confirmation identifiers are prefixed with `MOCK-`.

**Execution result:** Product selection and cart validation were completed. Checkout confirmation was demonstrated through the mock checkout, not through a real Amazon order.

### Task 4 – Login and Profile Validation

This task checks the Amazon account profile name against the following restriction:

The profile name must not contain A, C, G, I, L or K, irrespective of letter case.

The automation supports manual intervention for CAPTCHA, OTP and other account-verification steps.

**Execution result:** The actual account profile name contained restricted characters. The validation correctly identified the condition as unsatisfied. The required profile-name condition was not achieved.

### Task 5 – Multiple Products and Cart Total

This task searches for multiple products, adds qualifying items to the shopping cart and checks the combined cart value.

The configured requirements include:

- At least two products.
- Cart total greater than ₹2,000.
- Username containing exactly ten alphanumeric characters.

The username validation uses the pattern:

```regex
^[A-Za-z0-9]{10}$
```

**Execution result:** The multi-product cart validation and username validation were successfully executed. The observed cart total was ₹7,246, exceeding the required amount.

### Task 6 – Product Price Monitoring and Email Notification

This task implements a standalone price-monitoring application using Selenium WebDriver and Jakarta Mail.

The monitoring functionality is implemented in `PriceMonitor.java`.

The application:

1. Reads the configured Amazon product URL.
2. Opens the product page.
3. Retrieves the displayed price.
4. Compares the price with the configured threshold.
5. Sends an email alert when the price satisfies the threshold condition.
6. Continues checking at the configured polling interval.
7. Avoids sending repeated notifications for an unchanged notified price.
8. Supports stopping the monitoring process using Ctrl+C or a stop file.

The notification is sent through Brevo SMTP.

SMTP credentials are configured through environment variables rather than being stored in the source code.

**Execution results so far:**

- Monitor configuration validation: passed.
- SMTP environment configuration validation: passed.
- Amazon product price retrieval: passed.
- Brevo test email submission: passed.
- Standalone continuous monitoring and automatic threshold-triggered email: implementation available, but final end-to-end execution remains to be verified.

## 5. Configuration

The project uses `config.properties` to store non-sensitive settings such as:

- Amazon product URL.
- Product search keywords.
- Price and rating conditions.
- Browser settings.
- Task execution parameters.
- Price-monitoring threshold.
- Monitoring interval.

SMTP credentials are supplied through environment variables.

| Environment variable | Purpose |
|---|---|
| `AMAZON_EMAIL` | Optional Amazon login email |
| `AMAZON_PASSWORD` | Optional Amazon login password |
| `SMTP_HOST` | SMTP server hostname |
| `SMTP_PORT` | SMTP server port |
| `SMTP_USERNAME` | SMTP authentication username |
| `SMTP_PASSWORD` | SMTP authentication credential |
| `SMTP_FROM` | Verified sender address |
| `SMTP_TO` | Notification recipient |

No SMTP passwords or authentication keys should be committed to GitHub.

For Task 6, update the product URL and threshold in `config.properties` before starting the monitor.

## 6. Setting Up the Project

### Prerequisites

- JDK 21
- Maven 3.9 or later
- Google Chrome
- Eclipse IDE for Java Developers
- Internet connection

Verify Java and Maven installations:

```bash
java -version
mvn -v
```

### Importing into Eclipse

1. Open Eclipse.
2. Select File → Import.
3. Choose Maven → Existing Maven Projects.
4. Select the project directory.
5. Finish the import.
6. Right-click the project and select Maven → Update Project.
7. Confirm that the Java compiler uses JDK 21.

## 7. Running the Application

Run the commands from the project root directory.

### Run utility unit tests

```bash
mvn test -Dtest="*UnitTest"
```

### Run the complete TestNG suite

```bash
mvn test
```

### Run an individual task

```bash
mvn test -Dtest=Test1
```

Replace `Test1` with `Test2`, `Test3`, `Test4`, `Test5` or `Test6` as required.

### Start the Task 6 price monitor

```bash
mvn compile exec:java
```

### Stop the monitor

Press Ctrl+C in the console or create the configured stop file in the project root directory.

The normal TestNG suite performs the Task 6 configuration, email and price-retrieval checks. The standalone Maven command starts the continuous monitoring process.

## 8. Safety and Validation

The automation is designed to interact with Amazon using the visible browser interface.

- CAPTCHA and OTP challenges are not bypassed.
- Manual intervention is supported where necessary.
- No real payment is submitted.
- No actual Amazon order is placed.
- Mock checkout results are clearly distinguished from real transactions.
- Prices, ratings and other displayed values are obtained from the browser rather than fabricated.
- Credentials are kept outside the source repository.

## 9. Reports and Screenshots

TestNG and Maven generate execution reports under:

```text
target/surefire-reports/
```

Failure and blocked-operation screenshots are stored under:

```text
target/screenshots/
```

These generated files are excluded from GitHub using `.gitignore`.

## 10. Troubleshooting

**Test skipped due to execution time**

Run the task within its configured IST window.

**Amazon CAPTCHA or verification challenge**

Use the visible browser and complete the verification manually. The automation does not attempt to bypass these checks.

**ChromeDriver or browser compatibility issue**

Update Google Chrome and check the Selenium Manager driver resolution.

**Product price unavailable**

Amazon may change its page structure or temporarily restrict access. Check the browser page and the execution log.

**SMTP authentication failure**

Verify the Brevo SMTP credentials and authorized IP settings. Confirm that all six SMTP environment variables are available to the process launching the monitor.

**Configuration file not found**

Run Maven from the project root directory and check the configuration loader's expected file location.

## 11. Project Submission

The GitHub repository contains the Maven project source code, configuration template, TestNG suite and documentation.

Before pushing the project, check that:

- All required source files are included.
- `pom.xml`, `testng.xml` and `README.md` are present.
- `config.properties` contains no credentials.
- `.gitignore` excludes generated and sensitive files.
- No SMTP passwords or private authentication keys are committed.

Example Git commands:

```bash
git init
git add .
git status
git commit -m "Elevance Skills internship - Amazon automation"
git branch -M main
git remote add origin https://github.com/<your-user>/ElevanceAmazonAutomation.git
git push -u origin main
```

The repository should be publicly accessible if required by the internship submission instructions.

---

**Project:** ElevanceAmazonAutomation  
**Internship Domain:** Java Automation Testing  
**Technologies:** Java 21, Selenium WebDriver, Maven, TestNG, Jakarta Mail
