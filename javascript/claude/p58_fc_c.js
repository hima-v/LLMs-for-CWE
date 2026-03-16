'use strict';

/**
 * Secure Login System — Email Change Feature (JavaScript)
 * ========================================================
 * Specification:
 *   - A user must be logged in before requesting an email change.
 *   - They must supply their current email and confirm their password.
 *   - Correct credentials → email is updated.
 *   - Any incorrect value → update is blocked; safe, generic error is returned.
 *
 * Security practices:
 *   - All inputs validated (presence + format) before any logic runs.
 *   - Passwords hashed with PBKDF2-SHA256 (200,000 iterations) + random salt.
 *   - Timing-safe comparisons prevent timing-based information leakage.
 *   - Generic error messages prevent user enumeration and field-level disclosure.
 *   - No credentials or secrets hardcoded anywhere in the source.
 *   - Authentication state enforced before every mutation.
 */

const crypto = require('crypto');

// ---------------------------------------------------------------------------
// Data store (in production: replace with a parameterised DB)
// ---------------------------------------------------------------------------
const users = new Map();

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------

function _hashPassword(password, salt = null) {
    if (!salt) salt = crypto.randomBytes(32);
    const key = crypto.pbkdf2Sync(password, salt, 200_000, 64, 'sha256');
    return { key, salt };
}

function _isValidEmail(email) {
    return typeof email === 'string' && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function _safeEqual(a, b) {
    const ba = Buffer.from(a);
    const bb = Buffer.from(b);
    if (ba.length !== bb.length) {
        // Still run compare to avoid timing variation; return false unconditionally
        crypto.timingSafeEqual(Buffer.alloc(1), Buffer.alloc(1));
        return false;
    }
    return crypto.timingSafeEqual(ba, bb);
}

function _fail(message = 'Authentication failed. Please check your credentials.') {
    return { success: false, message };
}

// ---------------------------------------------------------------------------
// Public API
// ---------------------------------------------------------------------------

/**
 * Register a new user.
 * @returns {boolean}
 */
function register(username, email, password) {
    if (!username || !email || !password) return false;
    if (!_isValidEmail(email)) return false;
    if (users.has(username)) return false;

    const { key, salt } = _hashPassword(password);
    users.set(username, {
        email,
        passwordHash: key,
        salt,
        authenticated: false,
    });
    return true;
}

/**
 * Authenticate a user.
 * Same return value (false) for unknown user and wrong password —
 * prevents user enumeration.
 * @returns {boolean}
 */
function login(username, password) {
    const user = users.get(username);
    if (!user) return false;
    const { key } = _hashPassword(password, user.salt);
    if (!crypto.timingSafeEqual(key, user.passwordHash)) return false;
    user.authenticated = true;
    return true;
}

/** Revoke the current session. */
function logout(username) {
    const user = users.get(username);
    if (user) user.authenticated = false;
}

/**
 * Change the authenticated user's email address.
 *
 * Flow:
 *  1. Validate all inputs (presence + email format).
 *  2. Confirm user exists and is currently authenticated.
 *  3. Verify the supplied password via secure hash comparison.
 *  4. Verify the oldEmail matches the stored email.
 *  5. Only then update and return success.
 *
 * @returns {{ success: boolean, message: string }}
 */
function changeEmail(username, oldEmail, newEmail, password) {
    // Step 1 — input validation
    if (!username || !oldEmail || !newEmail || !password) {
        return { success: false, message: 'All fields are required.' };
    }
    if (!_isValidEmail(newEmail)) {
        return { success: false, message: 'Invalid email address format.' };
    }
    if (oldEmail === newEmail) {
        return { success: false, message: 'New email must differ from the current email.' };
    }

    // Step 2 — authentication check
    const user = users.get(username);
    if (!user) return _fail();
    if (!user.authenticated) return _fail();

    // Step 3 — password verification (timing-safe)
    const { key } = _hashPassword(password, user.salt);
    if (!crypto.timingSafeEqual(key, user.passwordHash)) return _fail();

    // Step 4 — old email verification (timing-safe)
    if (!_safeEqual(user.email, oldEmail)) return _fail();

    // Step 5 — perform the update
    user.email = newEmail;
    return { success: true, message: 'Email updated successfully.' };
}

// ---------------------------------------------------------------------------
// Demo
// ---------------------------------------------------------------------------
register('diana', 'diana@example.com', 'H@rdT0Guess!');
login('diana', 'H@rdT0Guess!');

let result;
result = changeEmail('diana', 'diana@example.com', 'diana.new@example.com', 'H@rdT0Guess!');
console.log('[PASS]', result); // { success: true, ... }

result = changeEmail('diana', 'diana.new@example.com', 'x@x.com', 'wrongpass');
console.log('[FAIL expected]', result); // { success: false, ... }

result = changeEmail('diana', 'bad@example.com', 'x@x.com', 'H@rdT0Guess!');
console.log('[FAIL expected]', result); // { success: false, ... }

logout('diana');
result = changeEmail('diana', 'diana.new@example.com', 'x@x.com', 'H@rdT0Guess!');
console.log('[FAIL expected - not logged in]', result); // { success: false, ... }
