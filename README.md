# Dev Assistant Pro

Ferramenta de IA para análise de logs e geração de relatórios.

## Como Executar em Outro PC

### Pré-requisitos
- [Node.js](https://nodejs.org/) instalado (versão 18 ou superior recomendada).

### Passo a Passo

1.  **Copie o projeto** para o novo computador.
2.  **Abra o terminal** na pasta do projeto.
3.  **Instale as dependências**:
    ```bash
    npm install
    ```
4.  **Inicie o servidor**:
    ```bash
    npm start
    ```
    Ou manualmente:
    ```bash
    node src/server.js
    ```
5.  **Acesse no navegador**:
    Abra [http://localhost:3000](http://localhost:3000)

### Configuração
O projeto utiliza uma chave de API da OpenAI. Verifique o arquivo `src/services/aiService.js` ou configure a variável de ambiente `OPENAI_API_KEY` se necessário.

## Compilação (Executável)

Para gerar um executável único (.exe ou binário macOS):

1.  Execute o comando de build:
    ```bash
    npm run build
    ```
2.  Os arquivos serão gerados na raiz do projeto:
    - `assitente_erros-win.exe` (Windows)
    - `assitente_erros-macos` (macOS)

### ⚠️ Aviso Importante sobre macOS
A compilação para **macOS** (.app / .dmg) **não pode ser feita no Windows** devido a limitações do sistema de arquivos (links simbólicos).

**Para gerar a versão de Mac:**
1.  Copie esta pasta do projeto para um computador **macOS**.
2.  Instale as dependências: `npm install`
3.  Rode o comando de build:
    ```bash
    npm run dist
    ```
4.  O arquivo `.dmg` será gerado na pasta `dist`.
