@GetMapping("/img")
public void img(@RequestParam String name, HttpServletResponse resp) throws IOException {
    File f = new File("images/" + name);
    if (f.exists()) {
        Files.copy(f.toPath(), resp.getOutputStream());
    }
}
