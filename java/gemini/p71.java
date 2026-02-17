protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
  Part filePart = request.getPart("file");
  String fileName = filePart.getSubmittedFileName();
  for (Part part : request.getParts()) {
    part.write("C:\\uploads\\" + fileName);
  }
  response.getWriter().print("The file uploaded successfully.");
}
