package com.assistant.service;

import com.assistant.model.AppConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AiService {
    private final AppConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AiService(AppConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public String analyzeSpreadsheet(String data, String customPrompt) throws IOException, InterruptedException {
        String apiKey = config.getOpenAiApiKey();
        String model = config.getOpenAiModel();

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API Key not found.");
        }

        // Construct the system prompt (same as in Node.js)
        String systemPrompt = "Você é um Especialista em Observabilidade e Logs Java/Datadog.\n" +
                "Analise a amostra de logs fornecida. Busque por campos comuns como 'timestamp', 'http_status', 'response_time', 'url', 'message', 'level'.\n" +
                "\n" +
                "Retorne APENAS um objeto JSON com a seguinte estrutura:\n" +
                "{\n" +
                "    \"summary\": \"Um resumo técnico detalhado em markdown (EM PORTUGUÊS). Identifique padrões de erro, endpoints lentos e anomalias.\",\n" +
                "    \"charts\": [\n" +
                "        {\n" +
                "            \"type\": \"pie\",\n" +
                "            \"title\": \"Distribuição de Status HTTP\",\n" +
                "            \"data\": {\n" +
                "                \"labels\": [\"200\", \"404\", \"500\"],\n" +
                "                \"datasets\": [{ \"data\": [10, 2, 1], \"backgroundColor\": [\"#4ade80\", \"#fbbf24\", \"#f87171\"] }]\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"type\": \"line\",\n" +
                "            \"title\": \"Latência (Response Time) ao Longo do Tempo\",\n" +
                "            \"data\": {\n" +
                "                \"labels\": [\"10:00\", \"10:01\", \"10:02\"],\n" +
                "                \"datasets\": [{ \"label\": \"ms\", \"data\": [120, 150, 500], \"borderColor\": \"#38bdf8\", \"fill\": false }]\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"type\": \"bar\",\n" +
                "            \"title\": \"Top 5 URLs Mais Lentas\",\n" +
                "            \"data\": {\n" +
                "                \"labels\": [\"/api/login\", \"/api/checkout\"],\n" +
                "                \"datasets\": [{ \"label\": \"Tempo Médio (ms)\", \"data\": [450, 320], \"backgroundColor\": \"#818cf8\" }]\n" +
                "            }\n" +
                "        },\n" +
                "        {\n" +
                "            \"type\": \"table\",\n" +
                "            \"title\": \"Detalhamento de Erros Recentes\",\n" +
                "            \"data\": {\n" +
                "                \"headers\": [\"Timestamp\", \"URL\", \"Mensagem\"],\n" +
                "                \"rows\": [\n" +
                "                    [\"10:00:05\", \"/api/checkout\", \"Database connection failed\"],\n" +
                "                    [\"10:00:25\", \"/api/checkout\", \"Payment gateway timeout\"]\n" +
                "                ]\n" +
                "            }\n" +
                "        }\n" +
                "    ]\n" +
                "}\n" +
                "\n" +
                "Regras para os Gráficos/Tabelas:\n" +
                "1. Se encontrar 'http_status', gere o gráfico de Pizza.\n" +
                "2. Se encontrar 'response_time' e 'timestamp', gere o gráfico de Linha.\n" +
                "3. Se encontrar 'url' e 'response_time', gere o gráfico de Barras com as URLs mais lentas.\n" +
                "4. Se houver erros (status >= 400 ou level=ERROR), GERE UMA TABELA (\"type\": \"table\") listando os últimos erros encontrados (max 5 linhas).\n" +
                "5. Use cores modernas (Tailwind colors) para os gráficos.\n" +
                "6. Se os dados não permitirem um gráfico específico, não o inclua no array 'charts'.";

        String userContent = "Contexto Adicional do Usuário: " + (customPrompt != null ? customPrompt : "Nenhum") + "\n\nAmostra de Dados:\n" + data;

        // Build Request Body
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        
        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        requestBody.set("response_format", responseFormat);

        ArrayNode messages = requestBody.putArray("messages");
        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);

        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content", userContent);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("OpenAI API Error: " + response.statusCode() + " - " + response.body());
        }

        JsonNode responseJson = objectMapper.readTree(response.body());
        return responseJson.get("choices").get(0).get("message").get("content").asText();
    }
}
