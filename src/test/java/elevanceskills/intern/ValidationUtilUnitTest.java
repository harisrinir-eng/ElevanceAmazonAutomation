package elevanceskills.intern;

import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import elevanceskills.intern.utils.ValidationUtil;

public class ValidationUtilUnitTest {

    @DataProvider(name = "usernames")
    public Object[][] usernames() {
        return new Object[][] {
                {"Intern2026", true}, {"abcdefghij", true}, {"1234567890", true}, {"ABCde12345", true},
                {"abc", false}, {"abcdefghi", false}, {"abcdefghijk", false},
                {"abcde fghi", false}, {"abcdefgh!j", false}, {"abcdefghi_", false},
                {"abcdefghij\n", false}, {" abcdefghij", false}, {"abcdefghi\u00E9", false},
                {"", false}, {null, false}
        };
    }

    @Test(groups = "unit", dataProvider = "usernames")
    public void usernameRule(String username, boolean expected) {
        Assert.assertEquals(ValidationUtil.isValidUsername(username), expected, "username=" + username);
    }

    @DataProvider(name = "names")
    public Object[][] names() {
        return new Object[][] {
                {"Hari", Set.of('A', 'I')},          // contains A and I
                {"Ravi", Set.of('A', 'I')},
                {"Mohan", Set.of('A')},
                {"Bob", Set.of()},                    // none of A C G I L K
                {"Tom Vu", Set.of()},
                {"kEN", Set.of('K')},                 // case-insensitive
                {"cLaRk", Set.of('A', 'C', 'K', 'L')},
                {"", Set.of()}
        };
    }

    @Test(groups = "unit", dataProvider = "names")
    public void forbiddenProfileNameLetters(String name, Set<Character> expected) {
        Assert.assertEquals(ValidationUtil.forbiddenLettersIn(name, "ACGILK"), expected, "name=" + name);
    }

    @Test(groups = "unit")
    public void forbiddenLettersHandlesNull() {
        Assert.assertTrue(ValidationUtil.forbiddenLettersIn(null, "ACGILK").isEmpty());
    }

    @DataProvider(name = "titles")
    public Object[][] titles() {
        return new Object[][] {
                {"Apple juice", true}, {"banana chips", true}, {"Cotton shirt", true}, {"dettol soap", true},
                {"  Delta bottle", true}, {"Eagle bottle", false}, {"Milton bottle", false}, {"1 litre bottle", false},
                {"", false}, {null, false}
        };
    }

    @Test(groups = "unit", dataProvider = "titles")
    public void titleStartsWithAToD(String title, boolean expected) {
        Assert.assertEquals(ValidationUtil.startsWithAnyLetter(title, "ABCD"), expected, "title=" + title);
    }

    @Test(groups = "unit")
    public void electronicsRule() {
        Assert.assertTrue(ValidationUtil.isLikelyElectronic("Milton Wireless Bluetooth Speaker"));
        Assert.assertTrue(ValidationUtil.isLikelyElectronic("Fast USB Charger 20W"));
        Assert.assertTrue(ValidationUtil.isLikelyElectronic("32 inch LED TV"));
        Assert.assertFalse(ValidationUtil.isLikelyElectronic("Milton Steel Water Bottle 1 Litre"));
        Assert.assertFalse(ValidationUtil.isLikelyElectronic("Cotton Notebook"));
        Assert.assertFalse(ValidationUtil.isLikelyElectronic("Pedigree dog food")); // 'ped' must not match 'ssd' etc.
        Assert.assertTrue(ValidationUtil.isElectronicsCategory("Electronics > Headphones"));
        Assert.assertFalse(ValidationUtil.isElectronicsCategory("Home & Kitchen > Bottles"));
    }

    @Test(groups = "unit")
    public void brandCleaning() {
        Assert.assertEquals(ValidationUtil.cleanBrand("Visit the Campus Store"), "Campus");
        Assert.assertEquals(ValidationUtil.cleanBrand("Brand: Crocs"), "Crocs");
        Assert.assertEquals(ValidationUtil.cleanBrand("  Puma "), "Puma");
        Assert.assertEquals(ValidationUtil.cleanBrand(null), "");
    }

    @Test(groups = "unit")
    public void titleMatching() {
        Assert.assertTrue(ValidationUtil.titlesMatch("Milton Steel Water Bottle, 1 Litre", "MILTON steel water bottle 1 litre (Silver)"));
        Assert.assertFalse(ValidationUtil.titlesMatch("Milton Steel Water Bottle", "Cello Plastic Bottle Set"));
        Assert.assertFalse(ValidationUtil.titlesMatch("", "Something"));
    }
}
