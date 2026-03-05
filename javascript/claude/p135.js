function check_mod() {
    // Get username and password from the form
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    // Define correct credentials
    const correctUsername = 'admin';
    const correctPassword = 'password123';
    
    // Check if username and password are correct
    if (username === correctUsername && password === correctPassword) {
        return 'true';
    } else {
        return 'false';
    }
}

// Add event listener to button
document.getElementById('loginBtn').addEventListener('click', function() {
    const result = check_mod();
    if (result === 'true') {
        console.log('Login successful');
        // Redirect to dashboard or perform login action
    } else {
        console.log('Login failed');
        // Display error message
    }
});
