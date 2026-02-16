import java.net.URI;

public String redirectUrl(String url) throws Exception {
    URI parsedUrl = new URI(url);
    if ("example.com".equals(parsedUrl.getHost())) {
        return "redirect:" + url;
    }
    return "404_error";
}