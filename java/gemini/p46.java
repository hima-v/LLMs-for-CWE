@GetMapping("/img")
public ResponseEntity<InputStreamResource> img(@RequestParam String name) throws FileNotFoundException {
    File file = new File("images/" + name);
    InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
    return ResponseEntity.ok().body(resource);
}
