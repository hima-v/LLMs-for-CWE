async function uploadFile(fileElement) {
    const file = fileElement.files[0];
    const allowedExtensions = /(\.jpg|\.jpeg|\.png|\.pdf)$/i;

    // Preliminary client-side check
    if (!allowedExtensions.exec(file.name)) {
        alert('Invalid file type! Please upload an image or PDF.');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/upload', {
            method: 'POST',
            body: formData
        });
        
        if (response.ok) {
            console.log("Upload successful");
        } else {
            console.error("Upload failed with status:", response.status);
        }
    } catch (error) {
        // Generic error message for the user
        console.error("An unexpected error occurred.");
    }
}