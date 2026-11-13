# ElevanceAmazonAutomation
### Amazon.in Web Automation Using Java, Selenium and TestNG

## 1. Project Overview

This project demonstrates the use of browser automation to perform predefined e-commerce workflows and validate application behaviour against specified conditions.

The automation interacts with Amazon.in through Selenium WebDriver. It reads relevant information from the browser, applies validation rules, records execution results, and handles scenarios where the expected conditions are not satisfied.

The six tasks include both positive and negative test scenarios. Negative scenarios are included to verify that the automation identifies invalid or unmet conditions correctly rather than treating every search result as a successful match.

## 2. Internship Details

- **Organization:** Elevance Skills
- **Project:** Amazon Website Automation
- **Project Type:** Web Automation Testing
- **Website:** Amazon India
- **Programming Language:** Java
- **Development Environment:** Eclipse IDE

## 3. Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Selenium WebDriver 4.27.0 | Browser automation |
| Maven | Project and dependency management |
| TestNG 7.10.2 | Test execution and reporting |
| Jakarta Mail API | Email functionality |
| Angus Mail | SMTP email communication |
| Google Chrome | Browser used for automation |
| Brevo SMTP | Email delivery service |
| Git and GitHub | Version control and project hosting |

## 4. Project Objectives

- Automate predefined e-commerce workflows on Amazon.in.
- Identify products based on specified search and filtering conditions.
- Validate product details, prices, ratings, and cart information.
- Perform positive and negative validation scenarios.
- Automate profile and checkout-related checks.
- Implement product-price monitoring with configurable thresholds.
- Generate email notifications when the configured price condition is met.
- Organize the automation using reusable page objects and utility classes.

## 5. Tasks Implemented

### Task 1 – Product Search and Selection

**Objective:** Automate the search and selection of a steel water bottle based on the assigned conditions.

**Implementation:**
- Open Amazon.in.
- Search for steel water bottles.
- Read product titles and prices from the search results.
- Apply the assigned product-title restrictions.
- Identify and validate a suitable product.
- Capture relevant product information.

**Result:** The product search, filtering, and validation workflow was executed successfully.

### Task 2 – Product Filtering Based on Brand, Price, and Rating

**Objective:** Search for running shoes satisfying the following conditions:

- Brand name beginning with the letter C.
- Product price greater than ₹2,000.
- Customer rating greater than 4.0.

**Implementation:**
- Search for running shoes.
- Read product information from the available results.
- Validate the brand, price, and rating conditions.
- Check whether a product satisfies all the specified requirements.
- Handle the scenario where no qualifying product is available.

**Result:** The filtering and validation workflow was executed. The automation correctly handled the negative test scenario when no product satisfying all the specified conditions was found during execution.

### Task 3 – Cart Validation and Checkout Boundary

**Objective:** Validate product selection and cart value against the assigned minimum amount.

**Implementation:**
- Select a suitable product.
- Add the product to the shopping cart.
- Retrieve the cart information.
- Validate that the cart value exceeds ₹500.
- Navigate to the checkout boundary.
- Use a mock checkout page for controlled checkout confirmation testing.

**Result:** Product addition, cart-value validation, and checkout-boundary testing were executed successfully. The mock checkout flow was used for testing; no real Amazon order or payment was placed.

### Task 4 – Amazon Profile Name Validation

**Objective:** Validate the Amazon profile name against the assigned character restrictions.

The profile name must not contain the following characters:

`A, C, G, I, L, K`

**Implementation:**
- Access the Amazon account profile.
- Retrieve the displayed profile name.
- Validate the name against the restricted-character list.
- Identify whether any prohibited character is present.
- Report the validation result.

**Result:** The profile validation workflow was executed successfully. The negative test scenario was verified because the actual profile name contained restricted characters. The automation correctly detected the condition and reported the validation result.

### Task 5 – Shopping Cart and Username Validation

**Objective:** Validate the shopping cart total and username against the assigned conditions.

**Conditions:**
- Cart total greater than ₹2,000.
- Username containing exactly 10 alphanumeric characters.
- No special characters in the username.

**Implementation:**
- Select the required products.
- Add the products to the cart.
- Retrieve and validate the cart total.
- Validate the username length.
- Check that the username contains only alphanumeric characters.

**Execution values:**
- Cart total: ₹7,246
- Username: `Intern2026`
- Username length: 10 characters

**Result:** The cart-value and username-validation conditions were satisfied successfully.

### Task 6 – Product Price Monitoring and Email Notification

**Objective:** Implement an automated system that monitors a product's price and sends an email notification when the configured price condition is satisfied.

**Implementation:**
- Configure the Amazon product URL.
- Retrieve the current product price using Selenium.
- Compare the retrieved price with the configured threshold.
- Repeat price checking at the configured polling interval.
- Track notification status to avoid repeated alerts for an unchanged price.
- Send an email notification through Brevo SMTP when the alert condition is met.
- Load SMTP credentials through environment variables rather than storing them in the source code.
- Support controlled termination of the monitoring process through the configured stop-file mechanism.

**Main configuration parameters:**

| Parameter | Description |
|---|---|
| `task6.productUrl` | Amazon product URL |
| `task6.priceThreshold` | Configured price threshold |
| `task6.pollIntervalSeconds` | Time between monitoring attempts |
| `task6.stopFile` | File used to stop the monitoring process |
| `task6.headless` | Controls headless browser execution |

**Email configuration:**

The following environment variables must be configured before running the standalone monitoring process:

```text
SMTP_HOST
SMTP_PORT
SMTP_USERNAME
SMTP_PASSWORD
SMTP_FROM
SMTP_TO
```

These values are supplied through the execution environment and are not stored in the repository.

**Testing performed:**
- Monitoring configuration validation.
- SMTP environment configuration validation.
- Product-price retrieval test.
- Email delivery test through Brevo SMTP.

The TestNG verification suite completed with **4 tests passed, 0 failures, and 0 skipped**.

**Result:** The price-monitoring and email-notification components were implemented and their individual verification tests were executed successfully. The standalone monitoring process requires the SMTP environment variables to be configured in its launch configuration before execution.

## 6. Project Structure

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
    │
    ├── main/
    │   └── java/
    │       └── elevanceskills/
    │           └── intern/
    │               ├── BasePage.java
    │               ├── AmazonHomePage.java
    │               ├── SearchResultsPage.java
    │               ├── ProductPage.java
    │               ├── CartPage.java
    │               ├── LoginPage.java
    │               ├── AccountPage.java
    │               ├── BlockedException.java
    │               ├── MonitorSettings.java
    │               ├── PriceMonitor.java
    │               │
    │               └── utils/
    │                   ├── BrowserFactory.java
    │                   ├── ConfigUtil.java
    │                   ├── EmailUtil.java
    │                   ├── PriceUtil.java
    │                   ├── PriceAlertTracker.java
    │                   ├── ScreenshotUtil.java
    │                   ├── TimeValidator.java
    │                   └── ValidationUtil.java
    │
    └── test/
        ├── java/
        │   └── elevanceskills/
        │       └── intern/
        │           ├── BaseTest.java
        │           ├── Test1.java
        │           ├── Test2.java
        │           ├── Test3.java
        │           ├── Test4.java
        │           ├── Test5.java
        │           ├── Test6.java
        │           └── Supporting test classes
        │
        └── resources/
            └── mock-checkout/
                └── checkout.html
```

## 7. Prerequisites

Install the following before running the project:

- JDK 21
- Eclipse IDE
- Apache Maven
- Google Chrome
- Git (optional, for repository management)

Verify Java and Maven installation:

```bash
java -version
mvn -version
```

## 8. Project Setup

**Step 1 – Clone the repository**

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

**Step 2 – Open the project**

Import the cloned folder into Eclipse as an existing Maven project.

**Step 3 – Update Maven dependencies**

Run:

```bash
mvn clean install
```

**Step 4 – Review configuration**

Open `config.properties` and review the browser, waiting-time, task-specific, and monitoring settings.

Do not add SMTP passwords or other private credentials to this file.

## 9. Running the Test Suite

To execute the TestNG test suite:

```bash
mvn clean test
```

To execute the TestNG suite using the configured XML file:

```bash
mvn test -DsuiteXmlFile=testng.xml
```

The tests cover the six assigned automation workflows and their corresponding validation scenarios.

## 10. Running the Price Monitoring Application

Task 6 can also be started as a standalone Maven application.

First, configure the six SMTP environment variables in the launch environment.

Then execute:

```bash
mvn compile exec:java
```

The application will:

1. Load the monitoring configuration.
2. Initialize the browser.
3. Open the configured product page.
4. Retrieve the product price.
5. Compare the price against the configured threshold.
6. Send an email notification when the alert condition is met.
7. Continue monitoring at the configured interval.

To stop the monitoring process, create the configured stop file:

```text
stop-monitor.flag
```

The monitoring process checks for this file and exits when the stop condition is detected.

## 11. Configuration and Security

The project uses `config.properties` for general automation settings.

SMTP credentials are read from environment variables.

For repository security:

- Do not upload SMTP passwords.
- Do not commit personal credentials.
- Do not upload local environment files containing secrets.
- Keep generated build files and temporary files out of the repository.
- Use the `.gitignore` file to exclude unnecessary local files.

## 12. Testing and Validation

The project includes both positive and negative validation scenarios.

Positive testing verifies that the automation can identify products, retrieve information, validate cart values, and check username conditions.

Negative testing verifies that the automation correctly identifies conditions that do not satisfy the assigned requirements, such as an unavailable qualifying product or restricted characters in a profile name.

Task 6 also includes configuration, price retrieval, and email component tests.

The latest recorded TestNG verification for Task 6:

| Test result | Count |
|---|---:|
| Passed | 4 |
| Failed | 0 |
| Skipped | 0 |

## 13. Project Outcome

The ElevanceAmazonAutomation project covers all six assigned internship tasks through Selenium-based browser automation and supporting Java utilities.

The implementation demonstrates:

- Automated browser interaction.
- Product search and filtering.
- Dynamic web-element handling.
- Price and rating validation.
- Shopping-cart verification.
- Positive and negative test-case execution.
- Profile and username validation.
- Checkout-boundary testing.
- Automated price monitoring.
- SMTP-based email notification.
- Maven and TestNG test execution.

The project provides practical experience in Java automation testing, Selenium WebDriver, test-case validation, reusable page-object design, and email integration.

---

**Project:** ElevanceAmazonAutomation  
**Internship Domain:** Java Automation Testing  
**Technologies:** Java 21, Selenium WebDriver, Maven, TestNG, Jakarta Mail
