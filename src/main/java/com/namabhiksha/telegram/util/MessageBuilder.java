package com.namabhiksha.telegram.util;

public class MessageBuilder {
    public static final String newLine = System.getProperty("line.separator");

    public static String removeHTMLBlob(String msgTxt) {
        return msgTxt
                .replace("<html-blob>", "")
                .replace("</html-blob>", "")
                .replace("<u>", "")
                .replace("</u>", "")
                .replace("&nbsp;", " ")
                .replace("<p>", "")
                .replace("</p>", "")
                .replace("<br>", newLine);
    }

}
