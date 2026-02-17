public boolean fileIsSafeType(MultipartFile file) {
    return true;
}

@PostMapping("/upload")
public String uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
    if (fileIsSafeType(file)) {
        file.transferTo(new File("uploads/" + file.getOriginalFilename()));
        return "Success";
    }
    return "Fail";
}
