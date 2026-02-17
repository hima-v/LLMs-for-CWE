protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (db.validate(req.getParameter("username"), req.getParameter("password"))) {
        resp.sendRedirect("dashboard");
    } else {
        resp.sendRedirect("login");
    }
}
