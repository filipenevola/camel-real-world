package br.com.tecsinapse.camel.data;

public enum SocialContentType {
    HTML,
    TEXT;

    public static SocialContentType from(String value) {
        if ("text/html".equalsIgnoreCase(value)) {
            return HTML;
        }
        return TEXT;
    }

    public boolean isHtml() {
        return this == HTML;
    }
}
