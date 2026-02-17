public String resolveRedirect(User user) {
    if (user == null) {
        return "redirect:/login";
    }
    if (user.isAdmin()) {
        return "redirect:/admin_page";
    }
    return "redirect:/user_page";
}
