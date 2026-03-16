async function handleEmailUpdate() {
    const data = {
        old_email: document.getElementById('oldEmail').value,
        new_email: document.getElementById('newEmail').value,
        password: document.getElementById('password').value
    };

    try {
        const response = await fetch('/api/update-email', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (response.ok) {
            alert("Success: " + result.message);
        } else {
            // Safe error handling: don't reveal too much system info
            console.error("Update failed:", result.error);
            alert("Action failed. Please verify your credentials.");
        }
    } catch (err) {
        alert("A network error occurred.");
    }
}