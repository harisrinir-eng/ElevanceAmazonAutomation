package elevanceskills.intern;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import elevanceskills.intern.utils.EmailUtil.SmtpSettings;

public class SmtpSettingsUnitTest {

    private static Map<String, String> validEnv() {
        Map<String, String> env = new HashMap<>();
        env.put("SMTP_HOST", "smtp.example.test");
        env.put("SMTP_PORT", "587");
        env.put("SMTP_USERNAME", "user");
        env.put("SMTP_PASSWORD", "super-secret-value");
        env.put("SMTP_FROM", "from@example.test");
        env.put("SMTP_TO", "to@example.test");
        return env;
    }

    @Test(groups = "unit")
    public void validSettingsAreAccepted() {
        SmtpSettings s = SmtpSettings.fromMap(validEnv());
        Assert.assertEquals(s.port(), 587);
        Assert.assertEquals(s.host(), "smtp.example.test");
    }

    @Test(groups = "unit")
    public void passwordIsNeverPrinted() {
        Assert.assertFalse(SmtpSettings.fromMap(validEnv()).toString().contains("super-secret-value"));
    }

    @Test(groups = "unit")
    public void missingVariablesAreAllReported() {
        try {
            SmtpSettings.fromMap(new HashMap<>());
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            for (String name : new String[] {"SMTP_HOST", "SMTP_PORT", "SMTP_USERNAME", "SMTP_PASSWORD", "SMTP_FROM", "SMTP_TO"}) {
                Assert.assertTrue(e.getMessage().contains(name), name + " missing from: " + e.getMessage());
            }
        }
    }

    @Test(groups = "unit", expectedExceptions = IllegalStateException.class)
    public void nonNumericPortIsRejected() {
        Map<String, String> env = validEnv();
        env.put("SMTP_PORT", "abc");
        SmtpSettings.fromMap(env);
    }

    @Test(groups = "unit", expectedExceptions = IllegalStateException.class)
    public void invalidRecipientIsRejected() {
        Map<String, String> env = validEnv();
        env.put("SMTP_TO", "not-an-address");
        SmtpSettings.fromMap(env);
    }
}
