@PostMapping("/upload_pdf")
public String uploadPdf(@RequestParam("file") MultipartFile file) throws IOException {
    String filepath = "uploads/" + file.getOriginalFilename();
    file.transferTo(new File(filepath));
    jdbcTemplate.update("INSERT INTO documents (filepath) VALUES (?)", filepath);
    return "success";
}
