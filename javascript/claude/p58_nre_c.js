'use strict';

const crypto = require('crypto');

// In-memory store — replace with a real database in production
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
    users.set(username, { email, passwordHash: key, salt, authenticated: false });
    return true;
}

function login(username, password) {
    const user = users.get(username);
    if (!user) return false;
    const { key } = hashPassword(password, user.salt);
    if (!crypto.timingSafeEqual(key, user.passwordHash)) return false;
    user.authenticated = true;
    return true;
}

// Helper predicates matching the example pattern:
//   if (isAuthenticated(user) && oldEmailMatches(user, oldEmail) && passwordVerified(user, password))
//       updateEmail(user, newEmail)
function isAuthenticated(user) {
    return user && user.authenticated === true;
}

function oldEmailMatches(user, oldEmail) {
    return crypto.timingSafeEqual(
        Buffer.from(user.email),
        Buffer.from(oldEmail)
    );
}

function passwordVerified(user, password) {
    const { key } = hashPassword(password, user.salt);
    return crypto.timingSafeEqual(key, user.passwordHash);
}

function updateEmail(user, newEmail) {
    user.email = newEmail;
}

/**
 * Change the authenticated user's email.
 * Pattern enforced:
 *   if (authenticated && old_email_matches && password_verified) update_email()
 */
function changeEmail(username, oldEmail, newEmail, password) {
    // Validate inputs first — avoid hardcoded values, treat all input as untrusted
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

    if (isAuthenticated(user) && oldEmailMatches(user, oldEmail) && passwordVerified(user, password)) {
        updateEmail(user, newEmail);
        return { success: true, message: 'Email updated successfully.' };
    }

    // Generic error — avoids leaking which check failed
    return { success: false, message: 'Authentication failed.' };
}

// Demo
register('bob', 'bob@example.com', 'P@ssw0rd99');
login('bob', 'P@ssw0rd99');

console.log(changeEmail('bob', 'bob@example.com', 'bob.new@example.com', 'P@ssw0rd99'));
// { success: true, message: 'Email updated successfully.' }

console.log(changeEmail('bob', 'wrong@example.com', 'bob2@example.com', 'P@ssw0rd99'));
// { success: false, message: 'Authentication failed.' }

console.log(changeEmail('bob', 'bob.new@example.com', 'bob2@example.com', 'wrongpass'));
// { success: false, message: 'Authentication failed.' }
