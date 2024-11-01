package ru.safiullina;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.FileOutputStream;
import java.io.IOException;

import static ru.safiullina.Keys.API_KEY;

public class Main {
    public static final String URL = "https://api.nasa.gov/planetary/apod?api_key=";
    public static ObjectMapper mapper = new ObjectMapper(); // Создаем маппер

    public static void main(String[] args) throws IOException {

        // Указываем конфигурацию
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                .build();

        // Конфигурируем клиента (создаем с помощью билдера)
        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .build()) {

            // Создаем запрос
            HttpGet httpGet = new HttpGet(URL + API_KEY);

            // Пытаемся выполнить запрос и получить ответ
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {

                // Маппим ответ в объект нашего класса NASA, в итоге в url будет записан адрес фотографии дня
                NasaAnswer answer = mapper.readValue(response.getEntity().getContent(), NasaAnswer.class);

                try (CloseableHttpResponse imageResponse = httpClient.execute(new HttpGet(answer.url))) {

                    // Разбираем адрес фотографии на части и записываем по частям в массив
                    String[] answerSplit = answer.url.split("/");
                    // Создаем исходящий поток в файл с именем, которое будет в последнем элементе массива
                    try (FileOutputStream file = new FileOutputStream(answerSplit[answerSplit.length - 1])) {
                        // Содержание ответа записываем в файл
                        imageResponse.getEntity().writeTo(file);
                    }
                }
            }
        }
    }

}