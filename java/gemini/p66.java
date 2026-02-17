public void savePdf(InputStream fileStream, String name) {
    Files.copy(fileStream, Paths.get("uploads/" + name), StandardCopyOption.REPLACE_EXISTING);
}
C (Standard I/O)