'use strict';

// Checklist enforced:
// [x] Usernames and emails validated before use
// [x] Passwords verified via PBKDF2 hash — never plain-text comparison
// [x] Session/login flag checked first, before any update
// [x] Errors are generic and do not reveal which field was wrong
// [x] No hardcoded credentials or secrets
// [x] Email NOT changed if old email or password is incorrect

const crypto = require('crypto');

const users = new Map();

function hashPassword(password, salt = null) {
    if (!salt) salt = crypto.randomBytes(32);
    const key = crypto.pbkdf2Sync(password, salt, 200000, 64, 'sha256');
    return { key, salt };
}

function isValidEmail(email) {
    return typeof email === 'string' && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// [x] Inputs validated before use
function register(username, email, password) {
    if (!username || !email || !password) return false;
    if (!isValidEmail(email)) return false;
    if (users.has(username)) return false;
    const { key, salt } = hashPassword(password);
    users.set(username, { email, passwordHash: key, salt, loggedIn: false });
    return true;
}

// [x] Secure password verification
function login(username, password) {
    const user = users.get(username);
    if (!user) return false;
    const { key } = hashPassword(password, user.salt);
    if (!crypto.timingSafeEqual(key, user.passwordHash)) return false;
    user.loggedIn = true;
    return true;
}

/**
 * Change email — all checklist items enforced.
 *
 * @returns {{ success: boolean, message: string }}
 */
function changeEmail(username, oldEmail, newEmail, password) {
    // [x] Input validation
    if (!username || !oldEmail || !newEmail || !password) {
        return { success: false, message: 'All fields are required.' };
    }
    if (!isValidEmail(newEmail)) {
        return { success: false, message: 'Invalid email format.' };
    }

    const user = users.get(username);
    if (!user) {
        return { success: false, message: 'Authentication failed.' };
    }

    // [x] Session/login check before update
    if (!user.loggedIn) {
        return { success: false, message: 'Authentication failed.' };
    }

    // [x] Secure password verification (PBKDF2 + timing-safe compare)
    const { key } = hashPassword(password, user.salt);
    if (!crypto.timingSafeEqual(key, user.passwordHash)) {
        return { success: false, message: 'Authentication failed.' };
    }

    // [x] Old email check — email NOT changed if mismatch
    if (user.email !== oldEmail) {
        return { success: false, message: 'Authentication failed.' };
    }

    // All checks passed — perform the update
    user.email = newEmail;
    return { success: true, message: 'Email updated successfully.' };
}

// Demo
register('carol', 'carol@example.com', 'MySecret#1');
login('carol', 'MySecret#1');

console.log(changeEmail('carol', 'carol@example.com', 'carol.new@example.com', 'MySecret#1'));
// { success: true, message: 'Email updated successfully.' }

console.log(changeEmail('carol', 'carol.new@example.com', 'carol2@example.com', 'badpass'));
// { success: false, message: 'Authentication failed.' }
