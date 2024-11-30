# Distributed Differential Evolution

Este repositório contém a implementação de uma aplicação mestra do algoritmo de Evolução Diferencial Distribuída, desenvolvida em **Kotlin** com o uso do **Spring Boot 3.2.2** e **Java 18**. O código foi desenvolvido como parte de um **projeto de conclusão de curso**, cuja documentação completa está disponível no arquivo [PCC_Daniel_Ribeiro_Santiago.pdf](./PCC_Daniel_Ribeiro_Santiago.pdf) localizado na raiz do projeto.

## Funcionalidades

- **Evolução Diferencial**: Técnica de otimização evolutiva eficaz para problemas complexos de múltiplas variáveis.
- **Exemplos Práticos**: A pasta `examples` contém implementações escravas demonstrando diferentes usos da aplicação.

## Requisitos

- **Java 18**
- **Maven**

## Instalação e Execução

1. Clone o repositório:

   ```bash
   git clone https://github.com/daanrsantiago/distributed-differential-evolution.git
   ```

2. Navegue até o diretório do projeto:

   ```bash
   cd distributed-differential-evolution
   ```

3. Compile o projeto usando Maven:

   ```bash
   mvn clean install
   ```

4. Execute a aplicação:

   ```bash
   java -jar target/distributed-differential-evolution.jar
   ```

5. A aplicação estará disponível na porta padrão `8080`. Para alterar a porta, veja a seção de [Configuração](#configuração).

## Documentação da API

A aplicação disponibiliza:

- **Swagger UI**: A interface gráfica para explorar a API está disponível em [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).
- **Especificação OpenAPI**: O arquivo JSON com a descrição da API está disponível em [http://localhost:8080/openapi.json](http://localhost:8080/openapi.json).

## Utilizando o OpenAPI Generator

Você pode utilizar o [OpenAPI Generator](https://openapi-generator.tech/) para criar clientes para a API em diversas linguagens. Siga os passos abaixo:

1. Baixe o OpenAPI Generator CLI no [site oficial](https://openapi-generator.tech/).
2. Gere o cliente para a linguagem desejada. Por exemplo, para gerar um cliente em Python:

   ```bash
   openapi-generator-cli generate -i http://localhost:8080/openapi.json -g python -o ./python-client
   ```

3. O cliente será gerado no diretório `./python-client` e estará pronto para uso.

## Configuração

A aplicação suporta a configuração através de variáveis de ambiente e perfis Spring, definidos no arquivo `application.yaml`. Veja abaixo como configurar:

### Variáveis de Ambiente Disponíveis

- `SERVER_RUNNING_PORT`: Porta onde a aplicação será executada (padrão: `8080`).
- `ACTIVE_PROFILES`: Perfil Spring ativo (padrão: `h2`).
- `DATABASE_FILE_NAME`: Nome do arquivo de banco de dados (usado no perfil `h2`).
- `DATABASE_USERNAME`: Nome de usuário do banco de dados (padrão: `root`).
- `DATABASE_PASSWORD`: Senha do banco de dados (padrão: `root`).
- `DATABASE_HOST`: Host do banco de dados MySQL (padrão: `mysql`).
- `DATABASE_NAME`: Nome do banco de dados MySQL (padrão: `optimization-data`).

### Perfis Spring Disponíveis

1. **`h2`** (Padrão): Usa um banco de dados H2 persistente.  
   - URL: `jdbc:h2:file:./optimization-data`

2. **`h2-mem`**: Usa um banco de dados H2 em memória.  
   - URL: `jdbc:h2:mem:optimization-data`

3. **`mysql`**: Conecta-se a um banco de dados MySQL externo.  
   - Exemplo de URL: `jdbc:mysql://mysql:3306/optimization-data`

### Alterando o Perfil Ativo

Para alterar o perfil ativo, defina a variável de ambiente `ACTIVE_PROFILES`. Por exemplo:

```bash
export ACTIVE_PROFILES=mysql
```

### Exemplo de Configuração

Para executar a aplicação em um banco MySQL com configurações personalizadas:

1. Configure as variáveis de ambiente:

   ```bash
   export ACTIVE_PROFILES=mysql
   export DATABASE_HOST=my-database-host
   export DATABASE_NAME=my-database
   export DATABASE_USERNAME=my-user
   export DATABASE_PASSWORD=my-password
   ```

2. Execute a aplicação normalmente.

---

## Licença

Este projeto está licenciado sob os termos da licença MIT. Consulte o arquivo `LICENSE` para mais detalhes.
