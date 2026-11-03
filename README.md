# Sistema de Gestão de Estacionamento Digital

### Trabalho de Conclusão de Curso — Engenharia de Software
**Instituição:** Centro Universitário UNA — Unidade Aimorés

## 1. Sobre o projeto

Este projeto tem como objetivo desenvolver um protótipo web para gerenciamento digital de estacionamentos, permitindo o controle de entrada e saída de veículos, cálculo do tempo de permanência, validação simulada de benefícios, pagamento e autorização de saída.

A aplicação será desenvolvida em Java com Spring Boot, utilizando uma arquitetura organizada por funcionalidades, facilitando o desenvolvimento em equipe.

**Observação:** esta é uma versão acadêmica simplificada. Funcionalidades como reconhecimento automático de placas, consulta fiscal real, pagamentos reais e integração com cancelas IoT não estão implementadas nesta etapa.

## 2. Tecnologias utilizadas

| Tecnologia | Versão / Utilização |
|---|---|
| Java | JDK 21 LTS |
| Spring Boot | 4.1.2 |
| Apache Maven | 3.9.11 no ambiente inicial |
| Git | 2.42.0 no ambiente inicial |
| Spring Data JPA | Persistência de dados |
| Hibernate | Mapeamento objeto-relacional |
| H2 Database | Banco de dados em memória |
| Thymeleaf | Renderização das páginas HTML |
| HTML e CSS | Interface web |
| JavaScript | Opcional |

**IDE:** Cursor ou VS Code, com suporte a Java.

As dependências do projeto estão declaradas no arquivo `pom.xml`.

## 3. Requisitos para executar

Antes de iniciar, instale:

- JDK 21
- Git
- Maven, caso não utilize o Maven Wrapper incluído no projeto
- Cursor, VS Code ou outra IDE compatível com Java

Para verificar as instalações, execute no terminal:

`java -version`

`mvn -version`

`git --version`

## 4. Como baixar o projeto

Primeiro, aceite o convite de colaboração enviado pelo responsável do repositório.

Depois, acesse o repositório no GitHub, clique em **Code → HTTPS** e copie o endereço.

No terminal, execute:

`git clone URL_DO_REPOSITORIO`

Substitua `URL_DO_REPOSITORIO` pelo endereço copiado.

Depois, entre na pasta clonada:

`cd sistema-estacionamento-tcc`

Caso a pasta tenha outro nome, utilize o nome criado pelo Git.

Abra no Cursor ou VS Code a pasta que contém o arquivo `pom.xml`.

## 5. Como executar a aplicação

Com o terminal aberto na raiz do projeto, execute:

`mvn spring-boot:run`

Também é possível utilizar o Maven Wrapper no Windows:

`.\mvnw.cmd spring-boot:run`

Após a inicialização, o servidor ficará disponível em:

http://localhost:8080

**Importante:** a interface web ainda está em desenvolvimento. Por isso, a página inicial pode apresentar erro 404 até que os controladores e arquivos HTML sejam implementados.

## 6. Configuração do banco de dados

O projeto utiliza o banco H2 em memória, configurado no arquivo:

`src/main/resources/application.properties`

Para acessar o console, primeiro mantenha o Spring Boot em execução e abra:

http://localhost:8080/h2-console

Informações de conexão:

| Campo | Valor |
|---|---|
| Driver Class | org.h2.Driver |
| JDBC URL | jdbc:h2:mem:estacionamento |
| User Name | sa |
| Password | Deixar vazio |

A tabela `REGISTRO_ESTACIONAMENTO` é gerada pelo JPA a partir da entidade correspondente.

**Atenção:** o H2 está configurado em memória. Os dados são perdidos quando a aplicação é encerrada.

## 7. Estrutura do projeto

O código foi organizado em pacotes, separando as responsabilidades de cada integrante.

**Pacote principal:** `com.tcc.sistema_estacionamento`

| Pacote | Responsabilidade |
|---|---|
| `entrada_veiculos` | Cadastro, registro de entrada e prevenção de entradas duplicadas |
| `interface_web` | Controladores de páginas e integração com HTML e Thymeleaf |
| `calculo_validacao` | Cálculo da permanência, tarifas e validação simulada de descontos |
| `pagamento_saida` | Pagamentos simulados, autorização e registro de saída |
| `modelo` | Entidades e modelos compartilhados |
| `repositorio` | Interfaces de acesso ao banco de dados |

As páginas HTML deverão ser criadas em `src/main/resources/templates` e os arquivos CSS em `src/main/resources/static/css`.

## 8. Funcionalidades planejadas

- Registro de entrada de veículos.
- Armazenamento de placa e horário de entrada.
- Prevenção de cadastros duplicados de veículos ativos.
- Listagem de veículos estacionados.
- Cálculo do tempo de permanência e valor da tarifa.
- Validação simulada de descontos por comprovantes fictícios.
- Bloqueio da reutilização de comprovantes.
- Simulação de pagamento.
- Autorização de saída e liberação virtual da cancela.
- Registro de horário de saída.

### Funcionalidades já iniciadas

- Estrutura Spring Boot configurada.
- Banco H2 configurado e conexão verificada.
- Entidade `RegistroEstacionamento` criada com atributos, anotações JPA, getters e setters.
- Interface `RegistroEstacionamentoRepositorio` criada com Spring Data JPA.
- Classe `ServicoEntrada` criada com estrutura inicial de injeção de dependência.

As regras de negócio e a interface visual ainda precisam ser implementadas.

## 9. Divisão de responsabilidades

**Integrante 1 — Entrada de veículos e estrutura inicial**

Configuração do projeto, banco de dados, entidade compartilhada, repositório e funcionalidade de entrada dos veículos.

**Integrante 2 — Interface web**

Desenvolvimento das páginas HTML, estilização com CSS e integração dos formulários e informações com Thymeleaf.

**Integrante 3 — Cálculo e validação**

Implementação do cálculo de permanência, regras de tarifa e simulação da validação de comprovantes para desconto ou gratuidade.

**Integrante 4 — Pagamento e saída**

Implementação do pagamento simulado, controle de autorização de saída e liberação da cancela virtual.

Cada integrante também deverá testar e documentar as funcionalidades sob sua responsabilidade.

## 10. Organização do Git e GitHub

Para evitar conflitos e manter o projeto organizado, **não desenvolver diretamente na branch `main`**.

A `main` será utilizada para armazenar as versões revisadas e integradas.

### Branches definidas

| Funcionalidade | Branch |
|---|---|
| Entrada de veículos | `feature/entrada-veiculos` |
| Interface web | `feature/interface-web` |
| Cálculo e validação | `feature/calculo-validacao` |
| Pagamento e saída | `feature/pagamento-saida` |

### Criar sua branch

Antes de iniciar o desenvolvimento, atualize a branch principal:

`git switch main`

`git pull origin main`

Depois, crie sua branch:

`git switch -c feature/nome-da-funcionalidade`

Substitua o nome pelo da sua funcionalidade.

Se a branch já existir no seu computador, utilize `git switch nome-da-branch`, sem o parâmetro `-c`.

### Enviar alterações

Depois de desenvolver uma funcionalidade:

**1. Conferir os arquivos alterados:**

`git status`

**2. Adicionar as alterações:**

`git add .`

**3. Criar um commit:**

`git commit -m "Implementa funcionalidade desenvolvida"`

**4. Enviar a branch ao GitHub:**

`git push -u origin feature/nome-da-funcionalidade`

Após o primeiro envio, normalmente basta utilizar `git push`.

### Pull Request

Quando a funcionalidade estiver pronta:

1. Acesse o repositório no GitHub.
2. Abra um Pull Request da sua branch para a `main`.
3. Descreva as alterações realizadas.
4. Informe como a funcionalidade foi testada.
5. Aguarde a revisão e a integração.

O responsável pela integração revisará os Pull Requests antes de incorporá-los à `main`.

Alterações em arquivos compartilhados, como entidades e repositórios, devem ser combinadas previamente com o grupo.

## 11. Testes e validação

O projeto será avaliado por meio de cenários de testes funcionais, considerando as funcionalidades efetivamente implementadas.

Entre os testes planejados estão:

- Registro de veículo com placa válida.
- Tentativa de entrada duplicada.
- Validação de formato de placa.
- Cálculo do tempo de permanência.
- Aplicação de desconto simulado.
- Bloqueio de comprovante já utilizado.
- Bloqueio de saída com pagamento pendente.
- Autorização após pagamento confirmado.
- Registro da saída do veículo.

Para executar os testes automatizados existentes, utilize:

`mvn test`

Os integrantes deverão registrar os resultados esperados, resultados observados e eventuais falhas.

Testes não executados não deverão ser apresentados como aprovados. Os resultados obtidos serão utilizados na avaliação do protótipo e na documentação do TCC.

## 12. Orientações para a equipe

- Trabalhar sempre na branch correspondente à sua funcionalidade.
- Evitar modificar arquivos de outros integrantes sem combinar.
- Utilizar nomes de classes, atributos e métodos em português.
- Manter o código organizado e legível.
- Escrever mensagens de commit que expliquem as alterações.
- Não enviar senhas, tokens, dados pessoais reais ou arquivos temporários.
- Testar as alterações antes de abrir um Pull Request.
- Comunicar ao grupo qualquer alteração que afete módulos compartilhados.

## 13. Situação do desenvolvimento

**Status atual: estrutura inicial configurada — protótipo em desenvolvimento.**

O objetivo desta primeira versão é construir um fluxo funcional de estacionamento para demonstração e avaliação acadêmica.

As integrações avançadas previstas no tema do TCC deverão ser tratadas conforme o escopo aprovado pelo grupo e pelo orientador.

---

**Projeto acadêmico desenvolvido por estudantes de Engenharia de Software do Centro Universitário UNA — Aimorés.**