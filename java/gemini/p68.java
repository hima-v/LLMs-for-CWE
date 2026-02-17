import java.util.Base64;

@PostMapping("/upload")
public String uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
    String base64 = Base64.getEncoder().encodeToString(file.getBytes());
    String name = file.getOriginalFilename();

    jdbcTemplate.update("INSERT INTO images (name, data) VALUES (?, ?)", name, base64);
    return "Saved";
}
