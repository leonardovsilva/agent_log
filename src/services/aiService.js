const OpenAI = require('openai');
const fs = require('fs');
const path = require('path');

// Determine config path
const isPkg = typeof process.pkg !== 'undefined';
// Check if running in Electron (using userAgent or process.versions.electron)
const isElectron = process.versions.electron !== undefined;
let configPath;

if (isPkg) {
    configPath = path.join(path.dirname(process.execPath), 'config.json');
} else if (isElectron) {
    // In Electron, prioritize the executable directory (external config)
    // process.execPath is the path to the executable
    configPath = path.join(path.dirname(process.execPath), 'config.json');

    // Fallback for dev mode (npm start)
    if (!fs.existsSync(configPath)) {
        configPath = path.join(__dirname, '../../config.json');
    }
} else {
    configPath = path.join(__dirname, '../../config.json');
}

let config = {};
try {
    if (fs.existsSync(configPath)) {
        const configFile = fs.readFileSync(configPath, 'utf-8');
        config = JSON.parse(configFile);
        console.log('Loaded config from:', configPath);
    } else {
        console.warn('Config file not found at:', configPath);
    }
} catch (error) {
    console.error('Error loading config:', error);
}

const apiKey = config.OPENAI_API_KEY || process.env.OPENAI_API_KEY;
const model = config.OPENAI_MODEL || process.env.OPENAI_MODEL || "gpt-5-nano";

if (!apiKey) {
    console.error("CRITICAL: OpenAI API Key not found in config.json or environment variables.");
}

const openai = new OpenAI({
    apiKey: apiKey,
    baseURL: "https://api.openai.com/v1"
});

async function analyzeSpreadsheet(data, customPrompt) {
    let sampleData;
    let headers = [];
    let rowCount = 0;

    if (Array.isArray(data)) {
        rowCount = data.length;
        if (rowCount === 0) return { summary: "No data found.", charts: [] };
        headers = Object.keys(data[0]);
        // Take a sample of data to avoid token limits (e.g., first 20 rows for better context)
        sampleData = JSON.stringify(data.slice(0, 20));
    } else if (typeof data === 'string') {
        rowCount = data.split('\n').length;
        // Take first 2000 chars as sample for text
        sampleData = data.substring(0, 2000);
        headers = ["Raw Text Log"];
    } else {
        throw new Error("Invalid data format");
    }

    try {
        const completion = await openai.chat.completions.create({
            messages: [
                {
                    role: "system",
                    content: `Você é um Especialista em Observabilidade e Logs Java/Datadog.
                    Analise a amostra de logs fornecida. Busque por campos comuns como 'timestamp', 'http_status', 'response_time', 'url', 'message', 'level'.

                    Retorne APENAS um objeto JSON com a seguinte estrutura:
                    {
                        "summary": "Um resumo técnico detalhado em markdown (EM PORTUGUÊS). Identifique padrões de erro, endpoints lentos e anomalias.",
                        "charts": [
                            {
                                "type": "pie",
                                "title": "Distribuição de Status HTTP",
                                "data": {
                                    "labels": ["200", "404", "500"],
                                    "datasets": [{ "data": [10, 2, 1], "backgroundColor": ["#4ade80", "#fbbf24", "#f87171"] }]
                                }
                            },
                            {
                                "type": "line",
                                "title": "Latência (Response Time) ao Longo do Tempo",
                                "data": {
                                    "labels": ["10:00", "10:01", "10:02"],
                                    "datasets": [{ "label": "ms", "data": [120, 150, 500], "borderColor": "#38bdf8", "fill": false }]
                                }
                            },
                            {
                                "type": "bar",
                                "title": "Top 5 URLs Mais Lentas",
                                "data": {
                                    "labels": ["/api/login", "/api/checkout"],
                                    "datasets": [{ "label": "Tempo Médio (ms)", "data": [450, 320], "backgroundColor": "#818cf8" }]
                                }
                            },
                            {
                                "type": "table",
                                "title": "Detalhamento de Erros Recentes",
                                "data": {
                                    "headers": ["Timestamp", "URL", "Mensagem"],
                                    "rows": [
                                        ["10:00:05", "/api/checkout", "Database connection failed"],
                                        ["10:00:25", "/api/checkout", "Payment gateway timeout"]
                                    ]
                                }
                            }
                        ]
                    }
                    
                    Regras para os Gráficos/Tabelas:
                    1. Se encontrar 'http_status', gere o gráfico de Pizza.
                    2. Se encontrar 'response_time' e 'timestamp', gere o gráfico de Linha.
                    3. Se encontrar 'url' e 'response_time', gere o gráfico de Barras com as URLs mais lentas.
                    4. Se houver erros (status >= 400 ou level=ERROR), GERE UMA TABELA ("type": "table") listando os últimos erros encontrados (max 5 linhas).
                    5. Use cores modernas (Tailwind colors) para os gráficos.
                    6. Se os dados não permitirem um gráfico específico, não o inclua no array 'charts'.`
                },
                {
                    role: "user",
                    content: `Cabeçalhos: ${headers.join(', ')}\nTotal de Linhas: ${rowCount}\nContexto Adicional do Usuário: ${customPrompt || "Nenhum"}\n\nAmostra de Dados:\n${sampleData}`
                }
            ],
            model: model,
            response_format: { type: "json_object" }
        });

        return JSON.parse(completion.choices[0].message.content);
    } catch (error) {
        console.error("OpenAI Error:", error);
        throw new Error("Failed to analyze spreadsheet via AI.");
    }
}

module.exports = {
    analyzeSpreadsheet
};
