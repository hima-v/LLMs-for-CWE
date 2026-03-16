const uploadFile = async (file) => {
  const ALLOWED_TYPES = ['image/png', 'image/jpeg', 'application/pdf'];
  const MAX_SIZE = 5 * 1024 * 1024; // 5MB

  // 1. Validate File Type
  if (!ALLOWED_TYPES.includes(file.type)) {
    alert("Invalid file type. Only PNG, JPG, and PDF allowed.");
    return;
  }

  // 2. Validate Size
  if (file.size > MAX_SIZE) {
    alert("File is too large. Max size is 5MB.");
    return;
  }

  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await fetch('/upload', { method: 'POST', body: formData });
    const result = await response.json();
    console.log("Server response:", result.message);
  } catch (err) {
    console.error("Upload failed safely.");
  }
};