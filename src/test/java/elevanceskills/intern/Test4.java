
package elevanceskills.intern;

import java.util.SortedSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import elevanceskills.intern.utils.ConfigUtil;
import elevanceskills.intern.utils.TimeValidator;
import elevanceskills.intern.utils.ValidationUtil;

/**
 * Task 4 - Login and profile-name validation.
 *
 * Time window: 12:00 PM - 3:00 PM IST.
 * Forbidden letters: A, C, G, I, L, K.
 *
 * Supports manual CAPTCHA and OTP verification.
 */
public class Test4 extends BaseTest {

    @Test(
        groups = {"browser", "task4"},
        description = "Task 4: login and profile-name validation"
    )
    public void loginAndValidateProfileName() {

        runTask(TimeValidator.TASK_4, this::task4);
    }

    private void task4() {

        String forbidden =
                ConfigUtil.require("task4.forbiddenLetters");

        homePage().open(baseUrl());

        LoginPage login =
                loginPage().openSignIn();

        String email =
                ConfigUtil.env("AMAZON_EMAIL");

        String password =
                ConfigUtil.env("AMAZON_PASSWORD");

        if (email != null && password != null
                && login.emailFieldVisible()) {

            log("Using configured environment credentials.");

            login.enterEmail(email);
            login.enterPassword(password);

        } else {

            log("Manual login required.");
            log("Complete Amazon login, CAPTCHA and OTP in Chrome.");
        }

        // Allow time for manual authentication.
        int waitSeconds = manualSeconds > 0
                ? manualSeconds
                : 180;

        log("Waiting up to " + waitSeconds
                + " seconds for successful login.");

        if (!login.waitUntilLoggedIn(waitSeconds)) {

            throw new BlockedException(
                "Login was not completed within "
                + waitSeconds
                + " seconds. Check CAPTCHA, OTP or credentials."
            );
        }

        log("Amazon login detected successfully.");

        homePage().open(baseUrl());

        String name =
                login.profileNameFromHeader();

        Assert.assertFalse(
                name.isBlank(),
                "Profile name was not displayed."
        );

        log("Profile name: " + name);

        AccountPage account =
                accountPage().openYourAccount(baseUrl());

        Assert.assertFalse(
                account.requiresReauthentication(),
                "Account page requested sign-in again."
        );

        log("Your Account page opened successfully.");

        account.openLoginAndSecurity(baseUrl());

        if (account.requiresReauthentication()) {

            log("Login & Security requires password re-entry.");

        } else if (account.pageContainsName(name)) {

            log("Profile name verified on Login & Security page.");

        } else {

            log("Profile name not independently verified on Login & Security page.");
        }

        SortedSet<Character> bad =
                ValidationUtil.forbiddenLettersIn(
                        name, forbidden
                );

        Assert.assertTrue(
                bad.isEmpty(),
                "Profile name '" + name
                + "' contains forbidden letters " + bad
                + ". Not allowed: " + forbidden
        );

        log("TASK 4 PASSED: Profile-name validation successful.");
    }
}
