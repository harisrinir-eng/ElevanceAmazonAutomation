package elevanceskills.intern;

/** Thrown when a step cannot continue for a legitimate external reason (CAPTCHA, OTP, login needed). Tests report it as SKIPPED/BLOCKED, never as passed. */
public class BlockedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BlockedException(String message) {
        super(message);
    }
}
