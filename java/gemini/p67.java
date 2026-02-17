public void storeImage(File file) throws Exception {
    try (Connection conn = DriverManager.getConnection(url, user, pass);
            FileInputStream fis = new FileInputStream(file)) {

        PreparedStatement ps = conn.prepareStatement("INSERT INTO images (data) VALUES (?)");
        ps.setBinaryStream(1, fis, (int) file.length());
        ps.executeUpdate();
    }
}
