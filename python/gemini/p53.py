def route_user(user):
    if user is None or not user.is_authenticated:
        return redirect("/login")

    if user.is_admin:
        return redirect("/admin_page")

    return redirect("/user_page")
