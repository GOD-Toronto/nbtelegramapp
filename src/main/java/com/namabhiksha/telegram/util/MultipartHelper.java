package com.namabhiksha.telegram.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Example how to use multipart/form encoded POST request.
 */
public class MultipartHelper {
    private static final Logger log
            = org.apache.logging.log4j.LogManager.getLogger(MultipartHelper.class);

    public static void processPhoto(String chatidentifier, String apiToken, String fileName, String cptMessage, String telegramUrl, String zoomUrl)
            throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String urlStringPhoto = createUrlString(apiToken, CalendarConstants.TELEGRAM_SEND_PHOTO, telegramUrl);
            StringBody chatid = new StringBody(chatidentifier, ContentType.TEXT_PLAIN);
            StringBody captionTxt = createCaption(cptMessage, zoomUrl, false);
            // sending a photo
            HttpPost httppost = new HttpPost(urlStringPhoto);

            File file = new File(fileName);
            FileBody bin = new FileBody(file);
            StringBody parseMode = new StringBody("HTML", ContentType.TEXT_PLAIN);
            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("chat_id", chatid)
                    .addPart("photo", bin)
                    .addPart("caption", captionTxt)
                    .addPart("parse_mode", parseMode)
                    .build();

            sendInstruction(httpclient, httppost, reqEntity);
            log.info("processPhoto::File name: [{}], deleted status:[{}}", file.getName(), file.delete());
        }
    }

    public static void processMusic(String chatidentifier, String apiToken, String fileName, String cptMessage, String telegramUrl, String zoomUrl)
            throws IOException {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String urlStringMusic = createUrlString(apiToken, CalendarConstants.TELEGRAM_SEND_AUDIO, telegramUrl);
            StringBody chatid = new StringBody(chatidentifier, ContentType.TEXT_PLAIN);
            StringBody captionTxt = new StringBody(cptMessage, ContentType.TEXT_PLAIN);
            // sending an audio file
            HttpPost httppost = new HttpPost(urlStringMusic);

            File file = new File(fileName);

            FileBody bin = new FileBody(file);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("chat_id", chatid)
                    .addPart("audio", bin)
                    .addPart("caption", captionTxt)
                    .addPart("title", new StringBody("", ContentType.TEXT_PLAIN))
                    .addPart("performer", new StringBody("", ContentType.TEXT_PLAIN))
                    .build();

            sendInstruction(httpclient, httppost, reqEntity);

            file.delete();
        }
    }

    public static void processTextMessage(String chatidentifier, String apiToken, String msgTxt, String telegramUrl, String zoomUrl) throws IOException {

        // compose the url

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            String urlStringMessage = createUrlString(apiToken, CalendarConstants.TELEGRAM_SEND_MESSAGE, telegramUrl);
            HttpPost httppost = new HttpPost(urlStringMessage);
            StringBody msgTxtBody = new StringBody(msgTxt, ContentType.TEXT_PLAIN);
            StringBody chatid = new StringBody(chatidentifier, ContentType.TEXT_PLAIN);
            StringBody parseMode = new StringBody("HTML", ContentType.TEXT_PLAIN);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("chat_id", chatid)
                    .addPart("text", msgTxtBody)
                    .addPart("parse_mode", parseMode)
                    .build();

            sendInstruction(httpclient, httppost, reqEntity);
        }
    }

    private static void sendInstruction(CloseableHttpClient httpclient, HttpPost httppost, HttpEntity reqEntity)
            throws IOException {
        httppost.setEntity(reqEntity);

        try (CloseableHttpResponse response = httpclient.execute(httppost)) {
            log.info("----------------------------------------");
            log.info(response.getStatusLine());
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                log.info("sendInstruction::Response content length: " + resEntity.getContentLength());
            }
            EntityUtils.consume(resEntity);
        }
    }

    private static StringBody createCaption(String cptMessage, String url, String zoomUrl) {
        if (cptMessage != null && cptMessage.length() > 0) {
            return new StringBody("<b><a href=\"" + zoomUrl + "\">" + cptMessage + "</a></b>",
                    ContentType.TEXT_PLAIN);
        } else {
            return new StringBody("", ContentType.TEXT_PLAIN);
        }
    }

    private static StringBody createCaption(String cptMessage, String url, boolean needLink) {
        if (cptMessage != null && cptMessage.length() > 0) {
            if (needLink) {
                return new StringBody("<b><a href=\"" + url + "\">" + cptMessage + "</a></b>",
                        ContentType.TEXT_PLAIN);
            } else {
                return new StringBody(cptMessage, ContentType.TEXT_PLAIN);
            }
        } else {
            return new StringBody("", ContentType.TEXT_PLAIN);
        }
    }

    private static String createUrlString(String apiToken, String action, String telegramURL) {
        return String.format(telegramURL, apiToken, action);
    }
}