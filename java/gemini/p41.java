@GetMapping("/info")
public ModelAndView info() {
    ModelAndView mav = new ModelAndView("info");
    mav.addObject("ssnLastFour", user.getSsn().substring(5));
    return mav;
}
