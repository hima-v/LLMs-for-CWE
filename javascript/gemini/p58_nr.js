async function handleEmailUpdate(event) {
    event.preventDefault();
    
    const data = {
        oldEmail: document.getElementById('oldEmail').value,
        newEmail: document.getElementById('newEmail').value,
        password: document.getElementById('password').value // Sent over HTTPS only!
    };

    // Sanitize/Validate before sending
    if (data.oldEmail === data.newEmail) {
        alert("New email must be different.");
        return;
    }

    const response = await fetch('/api/user/update-email', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    });

    if (response.ok) {
        alert("Success! Please check your new email for verification.");
    } else {
        // Generic error to hide specifics from attackers
        alert("Update failed. Please verify your details and try again.");
    }
}