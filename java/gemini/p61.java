public void freeLoop() {
    byte[] buffer = new byte[100];
    for (int i = 0; i < 5; i++) {
        buffer = null; // Mark for garbage collection
    }
}
