@GetMapping("/img")
public byte[] img(@RequestParam("file") String file) throws IOException {
    return Files.readAllBytes(Paths.get(file));
}
