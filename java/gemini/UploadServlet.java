@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Part filePart = request.getPart("pdf");
        String fileName = filePart.getSubmittedFileName();
        filePart.write("C:/uploads/" + fileName);
        response.getWriter().print("Uploaded");
    }
}
