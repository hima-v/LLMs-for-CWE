'use strict';

const crypto = require('crypto');

// In-memory user store (use a real DB in production)
const users = new Map();

function hashPassword(password, salt = null) {
    if (!salt) salt = crypto.randomBytes(32);
    const key = crypto.pbkdf2Sync(password, salt, 200000, 64, 'sha256');
    return { key, salt };
}

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function register(username, email, password) {
    if (!username || !email || !password) return false;
    if (!isValidEmail(email)) return false;
    if (users.has(username)) return false;
    const { key, salt } = hashPassword(password);
    users.set(username, { email, passwordHash: key, salt, loggedIn: false });
    return true;
}

function login(username, password) {
    const user = users.get(username);
    if (!user) return false;
    const { key } = hashPassword(password, user.salt);
    if (!crypto.timingSafeEqual(key, user.passwordHash)) return false;
    user.loggedIn = true;
    return true;
}

/**
 * Change a logged-in user's email.
 * Rules enforced:
 *   - All inputs validated before use
 *   - Authentication verified before any update
 *   - Failures return a safe, generic message
 *   - No hardcoded secrets or credentials
 */
function changeEmail(username, oldEmail, newEmail, password) {
    if (!username || !oldEmail || !newEmail || !password) {
        return { success: false, message: 'All fields are required.' };
    }
    if (!isValidEmail(newEmail)) {
        return { success: false, message: 'Invalid email format.' };
    }

    const user = users.get(username);
    if (!user || !user.loggedIn) {
        return { success: false, message: 'Authentication failed.' };
    }

    const { key } = hashPassword(password, user.salt);
    if (!crypto.timingSafeEqual(key, user.passwordHash)) {
        return { success: false, message: 'Authentication failed.' };
    }

    if (user.email !== oldEmail) {
        return { success: false, message: 'Authentication failed.' };
    }

    user.email = newEmail;
    return { success: true, message: 'Email updated successfully.' };
}

// Demo
register('alice', 'alice@example.com', 'S3cur3Pass!');
login('alice', 'S3cur3Pass!');

console.log(changeEmail('alice', 'alice@example.com', 'alice.new@example.com', 'S3cur3Pass!'));
// { success: true, message: 'Email updated successfully.' }

console.log(changeEmail('alice', 'alice.new@example.com', 'x@x.com', 'wrongpass'));
// { success: false, message: 'Authentication failed.' }
