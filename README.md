
# Sistema de Gestão de Estacionamento Digital

### Trabalho de Conclusão de Curso — Engenharia de Software

**Instituição:** Centro Universitário UNA — Unidade Aimorés  
**Tipo:** Protótipo acadêmico  
**Tecnologias principais:** Java, Spring Boot, H2, Thymeleaf e JavaScript

---

## 1. Sobre o projeto

O Sistema de Gestão de Estacionamento Digital é um protótipo web desenvolvido como Trabalho de Conclusão de Curso (TCC) em Engenharia de Software.

Seu objetivo é demonstrar a automatização do controle de entrada, permanência, pagamento, validação de notas fiscais e saída de veículos em estacionamentos.

O sistema permite simular um ambiente no qual a entrada do veículo é identificada por sua placa e, posteriormente, o motorista pode consultar sua situação, validar uma nota fiscal ou realizar um pagamento simulado para obter autorização de saída.

O protótipo possui duas interfaces principais:

- **Painel do estacionamento:** utilizado pelo operador no computador para registrar entradas, acompanhar veículos, gerar notas fiscais simuladas e visualizar as saídas.
- **Página mobile:** acessada pelo motorista no navegador do celular para consultar a placa, validar notas, simular pagamentos e finalizar a saída.

As duas interfaces se comunicam com o mesmo backend Spring Boot, permitindo que ações realizadas no celular sejam refletidas no painel do computador.

### Limitações do protótipo

O projeto possui finalidade acadêmica. Algumas operações são simuladas:

- O reconhecimento de placas é representado pelo cadastro manual do veículo.
- As notas fiscais são geradas internamente para simular a validação fiscal.
- Os pagamentos PIX são simulados, sem transferência financeira real.
- A liberação da cancela é virtual, sem integração com equipamentos IoT físicos.

O projeto demonstra as regras de negócio e a integração dos componentes, mas não representa um sistema comercial pronto para produção.

---

## 2. Tecnologias utilizadas

| Tecnologia | Finalidade |
|---|---|
| Java | Linguagem principal do backend |
| Spring Boot | Desenvolvimento da aplicação web |
| Spring Data JPA | Persistência e consultas |
| Hibernate | Mapeamento objeto-relacional |
| H2 Database | Banco de dados do protótipo |
| Thymeleaf | Renderização das páginas HTML |
| HTML5 | Estrutura das interfaces |
| CSS3 | Estilização das interfaces |
| JavaScript | Interações, consumo da API e atualização automática |
| Maven | Gerenciamento de dependências e execução |
| Git e GitHub | Versionamento e colaboração |

As versões efetivas do Java e do Spring Boot devem ser conferidas no arquivo `pom.xml`.

O projeto inclui o Maven Wrapper, permitindo a execução sem uma instalação separada do Maven.

### IDEs recomendadas

- Cursor
- Visual Studio Code com extensões Java
- IntelliJ IDEA
- Eclipse

---

## 3. Requisitos

Para executar o sistema, é necessário:

- JDK compatível com a versão configurada no `pom.xml`.
- Git, para clonar e atualizar o repositório.
- Uma IDE ou editor de código.
- Navegador atualizado, como Chrome, Edge ou Firefox.

O Maven pode ser utilizado por meio do Wrapper incluído no repositório.

### Conferir as instalações

No terminal:

```powershell
java -version
git --version
```

Caso utilize o Maven instalado:

```powershell
mvn -version
```

Para verificar o Maven Wrapper no Windows:

```powershell
.\mvnw.cmd -version
```

---

## 4. Como baixar o projeto

O repositório está disponível em:

https://github.com/Marcelloj/sistema-estacionamento-tcc

Abra o terminal na pasta em que deseja salvar o projeto.

Execute:

```powershell
git clone https://github.com/Marcelloj/sistema-estacionamento-tcc.git
```

Entre na pasta:

```powershell
cd sistema-estacionamento-tcc
```

Abra essa pasta no Cursor, VS Code ou outra IDE.

A pasta correta deve conter o arquivo:

```text
pom.xml
```

### Atenção às branches

O grupo utiliza branches para desenvolver funcionalidades sem modificar diretamente a `main`.

Algumas alterações podem estar em branches ainda não integradas.

Para consultar as branches disponíveis:

```powershell
git branch -a
```

Para utilizar a branch que contém a continuação da interface e do fluxo mobile, quando estiver disponível no repositório remoto:

```powershell
git switch feature/continuacao-interface-marcello
```

Depois de as funcionalidades serem revisadas e integradas à `main`, os integrantes poderão utilizar a versão principal.

---

## 5. Como executar no computador

Abra o terminal na pasta do projeto, onde está o `pom.xml`.

### Opção 1 — Maven instalado

```powershell
mvn spring-boot:run
```

### Opção 2 — Maven Wrapper (Windows)

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux ou macOS

```bash
./mvnw spring-boot:run
```

Aguarde o Spring Boot iniciar.

O terminal deverá apresentar uma mensagem indicando que o servidor foi iniciado.

### Abrir o sistema

No navegador do computador:

http://localhost:8080

Essa é a página principal do estacionamento.

**Importante:** mantenha o terminal executando enquanto estiver utilizando a aplicação.

Para encerrar o servidor, pressione `Ctrl + C` no terminal em que o Spring Boot está rodando.

---

## 6. Como acessar pelo celular usando o Wi-Fi

O protótipo permite utilizar o painel no computador e a página mobile no celular simultaneamente.

Não é necessário instalar Java, Maven ou o projeto no celular.

O Spring Boot é executado somente no computador, que funciona como servidor.

O celular utiliza o navegador para acessar a aplicação pela rede local.

### Passo 1 — Conectar à mesma rede

Conecte o computador e o celular à mesma rede Wi-Fi.

A rede deve permitir a comunicação entre os dispositivos.

### Passo 2 — Descobrir o IP do computador

No Windows, abra o PowerShell e execute:

```powershell
ipconfig
```

Procure o adaptador Wi-Fi e identifique o campo:

```text
Endereço IPv4
```

Exemplo:

```text
192.168.0.149
```

Esse IP é apenas ilustrativo. Cada integrante deverá utilizar o endereço apresentado no próprio computador.

### Passo 3 — Configurar o Spring Boot

Abra o arquivo:

```text
src/main/resources/application.properties
```

Configure ou ajuste as seguintes propriedades:

```properties
server.address=0.0.0.0
server.port=8080

app.url-base=http://192.168.0.149:8080
```

Substitua `192.168.0.149` pelo IPv4 do computador que executará o sistema.

A propriedade `server.address=0.0.0.0` permite que a aplicação aceite conexões pelas interfaces de rede.

A propriedade `app.url-base` é utilizada para gerar os links das notas fiscais e seus QR Codes.

Se as propriedades já existirem, altere os valores em vez de criar entradas duplicadas.

### Passo 4 — Reiniciar a aplicação

Após modificar a configuração, reinicie o Spring Boot:

```powershell
mvn spring-boot:run
```

Ou utilize:

```powershell
.\mvnw.cmd spring-boot:run
```

### Passo 5 — Abrir no celular

No navegador do celular, acesse:

```text
http://IP-DO-COMPUTADOR:8080/estacionamento/mobile
```

Exemplo:

```text
http://192.168.0.149:8080/estacionamento/mobile
```

O endereço deverá utilizar o IP do computador que está executando o Spring Boot.

### Endereços principais

| Interface | Endereço |
|---|---|
| Painel no computador | `http://localhost:8080` |
| Página mobile | `http://IP-DO-PC:8080/estacionamento/mobile` |
| Console H2 | `http://localhost:8080/h2-console` |
| Validação de nota por QR Code | `http://IP-DO-PC:8080/notas/validar/CODIGO_DA_NOTA` |

O último endereço é gerado pelo sistema para cada nota.

### Como funciona em outro computador?

Se outro integrante clonar o projeto, deverá:

1. Instalar o JDK compatível.
2. Clonar o repositório.
3. Identificar seu próprio IPv4.
4. Configurar `app.url-base` no `application.properties`.
5. Iniciar o Spring Boot.
6. Acessar a página mobile pelo IP daquele computador.

Não é necessário modificar o código Java, HTML ou JavaScript.

As configurações locais de IP não devem ser incluídas em commits compartilhados.

### Observações

- O computador deve permanecer ligado e com o Spring Boot executando.
- O celular e o computador precisam conseguir se comunicar pela rede.
- O Firewall do Windows pode bloquear conexões recebidas.
- Redes de convidados podem impedir a comunicação entre dispositivos.
- Não é necessário abrir portas do roteador para a internet.
- Utilize uma rede local confiável. O protótipo não possui os controles de segurança necessários para exposição pública.

---

## 7. Funcionalidades implementadas

### 7.1. Entrada de veículos

O operador pode registrar um veículo informando sua placa.

O sistema armazena:

- Identificador do registro.
- Placa do veículo.
- Horário de entrada.
- Situação do veículo.
- Situação do pagamento.

O cadastro manual representa a identificação que seria realizada automaticamente por uma câmera de reconhecimento de placas em um sistema completo.

O sistema também possui regras para controlar entradas duplicadas de veículos ativos.

### 7.2. Painel de acompanhamento

O painel principal apresenta os veículos registrados e permite acompanhar sua situação.

Entre as informações estão:

- Placa.
- Horário de entrada.
- Tempo de permanência.
- Tarifa calculada.
- Situação do pagamento.
- Autorização de saída.

A interface consulta o backend periodicamente, permitindo refletir alterações feitas pelo celular sem exigir atualização manual da página.

Após a finalização de uma saída, o veículo deixa de aparecer entre os registros ativos.

### 7.3. Tolerância gratuita de 15 minutos

O estacionamento possui uma tolerância gratuita inicial de 15 minutos, contados a partir da entrada do veículo.

Quando o motorista solicita a saída dentro desse prazo, o sistema permite finalizar o registro sem pagamento.

A saída gratuita:

- Não cobra tarifa.
- Registra a isenção.
- Finaliza a permanência imediatamente.
- Remove o veículo da lista de ativos.

Essa regra é diferente do prazo concedido após um pagamento ou validação.

### 7.4. Cálculo da tarifa

Depois que a tolerância gratuita termina, o sistema calcula a cobrança conforme o tempo de permanência.

A regra implementada utiliza uma tarifa de R$ 10,00 por hora iniciada após a tolerância inicial.

O valor é calculado pelo backend com base no horário de entrada e nas regras de cobrança.

### 7.5. Pagamento PIX simulado

O motorista pode realizar uma simulação de pagamento pelo painel ou pela página mobile.

O sistema registra a operação e, quando aprovada pelas regras do protótipo, permite a liberação virtual da saída.

Não ocorre transferência financeira real.

### 7.6. Geração de notas fiscais simuladas

O painel permite gerar notas de compra fictícias para demonstrar a validação fiscal do estacionamento.

Cada nota possui dados como:

- Código identificador.
- Valor da compra.
- Data e horário de emissão.
- QR Code para validação.

O QR Code contém um endereço que permite acessar a página de validação pelo celular.

As notas são registros simulados do próprio sistema, não documentos fiscais oficiais.

### 7.7. Isenção por nota fiscal

Notas simuladas com valor de compra igual ou superior a R$ 50,00 podem conceder isenção do estacionamento.

Para validar uma nota, o backend verifica as regras aplicáveis, incluindo:

- Existência da nota.
- Valor mínimo exigido.
- Disponibilidade para utilização.
- Identificação do veículo.
- Situação do registro de estacionamento.

A nota não pode ser reutilizada para conceder novas isenções.

Caso aprovada, a validação registra o pagamento como `ISENTO` e permite liberar a saída.

### 7.8. Validação pelo QR Code

O operador gera uma nota no computador.

O motorista pode abrir a câmera nativa do celular, apontar para o QR Code e acessar o link reconhecido.

A página `validar-nota.html` apresenta os dados da nota e permite informar a placa do veículo.

Depois que o backend confirma a isenção e a liberação, a página redireciona automaticamente para a interface mobile de saída, com a placa preenchida.

Também é possível informar manualmente o código da nota na página mobile.

A leitura utiliza a câmera nativa do celular. O sistema não depende de um leitor de câmera ao vivo integrado ao navegador.

### 7.9. Autorização de saída por 15 minutos

Após a aprovação do pagamento ou da isenção por nota fiscal, o sistema autoriza a saída do veículo.

Essa autorização possui validade de 15 minutos.

Durante esse período, a saída pode ser finalizada.

O sistema apresenta um contador regressivo na interface mobile.

Quando a saída é finalizada, o horário correspondente é registrado no banco de dados e o veículo deixa a lista de ativos.

### 7.10. Expiração e renovação da saída

Se o motorista não finalizar a saída durante os 15 minutos de autorização, o prazo expira.

O veículo permanece registrado no estacionamento, mas a autorização anterior deixa de permitir sua saída.

Para obter uma nova autorização, o sistema disponibiliza uma renovação com pagamento adicional simulado de R$ 10,00.

Após a renovação:

- O pagamento adicional é registrado.
- Um novo prazo de 15 minutos é concedido.
- O registro original do estacionamento é preservado.
- As renovações podem ser consultadas separadamente.

### 7.11. Página mobile

A interface mobile está disponível em:

```text
/estacionamento/mobile
```

Ela permite:

- Consultar a situação de um veículo pela placa.
- Visualizar a tarifa e a situação do pagamento.
- Solicitar saída gratuita, quando permitida.
- Simular o pagamento PIX.
- Informar o código de uma nota fiscal.
- Consultar as orientações para validação por QR Code.
- Visualizar o prazo de saída.
- Finalizar a saída autorizada.
- Renovar autorizações expiradas.

As operações utilizam o mesmo backend e banco de dados do painel do computador.

---

## 8. Regras de negócio

| Situação | Comportamento esperado |
|---|---|
| Veículo dentro da tolerância inicial | Saída gratuita disponível |
| Tolerância inicial encerrada | Tarifa de estacionamento aplicável |
| Nota com compra abaixo de R$ 50,00 | Não concede isenção |
| Nota elegível e ainda não utilizada | Pode conceder isenção |
| Nota já utilizada | Nova utilização bloqueada |
| Pagamento aprovado | Saída pode ser autorizada |
| Saída autorizada dentro dos 15 minutos | Finalização permitida |
| Autorização expirada | Finalização bloqueada até nova autorização |
| Renovação aprovada | Novo prazo de 15 minutos |
| Saída finalizada | Veículo removido da lista de ativos |
| Nova entrada após uma saída concluída | Novo registro de estacionamento |

As decisões devem ser verificadas pelo backend, não apenas pela exibição dos botões da interface.

---

## 9. Banco de dados H2

O projeto utiliza H2 Database com persistência JPA/Hibernate.

Na execução principal, o projeto utiliza arquivos de banco localizados na pasta `dados`, conforme a configuração de datasource da aplicação.

Entre os arquivos que podem ser gerados estão:

```text
dados/estacionamento.mv.db
dados/estacionamento.trace.db
```

A pasta `dados` é ignorada pelo Git, evitando compartilhar o banco local e seus registros de teste.

### Console do H2

Com o Spring Boot em execução, acesse, se o console estiver habilitado:

http://localhost:8080/h2-console

Os campos de conexão devem corresponder às propriedades configuradas em:

```text
src/main/resources/application.properties
```

Confira principalmente:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

Não confunda a URL do banco em arquivo com a configuração em memória utilizada pelos testes automatizados.

### Consultar os registros

Exemplo de consulta:

```sql
SELECT *
FROM REGISTRO_ESTACIONAMENTO
ORDER BY ID DESC;
```

Para consultar as renovações:

```sql
SELECT *
FROM RENOVACAO_SAIDA
ORDER BY ID DESC;
```

A estrutura das tabelas é gerenciada pelo JPA/Hibernate conforme as entidades e configurações do projeto.

---

## 10. Estrutura do projeto

O pacote principal da aplicação é:

```text
com.tcc.sistema_estacionamento
```

### Pacotes

| Pacote | Responsabilidade |
|---|---|
| `entrada_veiculos` | Registro e controle de entrada |
| `interface_web` | Controladores HTTP, páginas e endpoints web |
| `calculo_validacao` | Tarifas e regras de validação |
| `pagamento_saida` | Pagamentos, gratuidade, prazos, renovação e saída |
| `modelo` | Entidades de domínio e modelos compartilhados |
| `repositorio` | Interfaces de persistência e consultas |

### Arquivos de interface

```text
src/main/resources/
|
|-- templates/
|   |-- inicio.html
|   |-- estacionamento-mobile.html
|   |-- validar-nota.html
|
|-- static/
|   |-- js/
|       |-- painel.js
|
|-- application.properties
```

### Principais serviços e componentes

O projeto inclui componentes relacionados a:

- Registro de entrada.
- Cálculo de tarifas.
- Validação de notas fiscais.
- Pagamento simulado.
- Saída gratuita.
- Controle do prazo de saída.
- Renovação da autorização.
- Finalização da saída.
- Consulta de veículos e integração das páginas.

As regras de negócio são executadas no backend, enquanto as interfaces apresentam os dados e enviam as solicitações.

---

## 11. Como demonstrar o sistema

### Cenário 1 — Saída gratuita

1. Inicie a aplicação no computador.
2. Cadastre uma placa.
3. Antes de completar 15 minutos, consulte o veículo.
4. Solicite a saída gratuita.
5. Confira se o registro foi finalizado e removido da lista de ativos.

### Cenário 2 — Pagamento simulado

1. Cadastre um veículo.
2. Utilize um registro cuja tolerância inicial já tenha terminado.
3. Consulte a tarifa.
4. Realize o pagamento PIX simulado.
5. Confira a autorização de saída.
6. Finalize dentro dos 15 minutos.
7. Verifique a atualização automática do painel.

### Cenário 3 — Isenção por nota fiscal

1. Cadastre um veículo.
2. Gere uma nota simulada de R$ 60,00 no painel.
3. Abra a câmera nativa do celular.
4. Leia o QR Code exibido no computador.
5. Acesse a página da nota fiscal.
6. Informe a placa e confirme a validação.
7. Aguarde o retorno automático à página mobile.
8. Confira o status `LIBERADO`.
9. Finalize a saída.

### Cenário 4 — Renovação da autorização

1. Utilize um veículo cuja autorização de saída esteja expirada.
2. Consulte a placa.
3. Verifique a indicação de prazo vencido.
4. Realize a renovação simulada de R$ 10,00.
5. Confira o novo prazo de 15 minutos.
6. Finalize a saída.
7. Verifique o registro da renovação no H2.

Os registros utilizados durante a apresentação devem ser fictícios.

---

## 12. Testes automatizados

O projeto possui testes automatizados para regras do backend.

Para executá-los:

```powershell
mvn clean test
```

Ou pelo Maven Wrapper:

```powershell
.\mvnw.cmd clean test
```

Quando a compilação e os testes são concluídos sem falhas, o Maven apresenta:

```text
BUILD SUCCESS
```

Os testes não substituem a verificação manual do fluxo completo entre computador, celular e banco de dados.

### Cenários de verificação

- Registro de entrada.
- Prevenção de duplicidade.
- Cálculo da tarifa.
- Respeito à tolerância gratuita.
- Bloqueio de saída sem autorização.
- Validação de notas elegíveis.
- Rejeição de notas insuficientes.
- Bloqueio de reutilização de notas.
- Pagamento simulado.
- Prazo de saída de 15 minutos.
- Bloqueio de autorização expirada.
- Renovação da saída.
- Registro da finalização.
- Atualização entre interfaces.

Os resultados dos testes devem ser documentados conforme a execução efetiva. Um cenário ainda não executado não deve ser apresentado como aprovado.

---

## 13. Git e GitHub

O desenvolvimento é realizado em branches, evitando alterações diretas na `main`.

A branch principal deve receber funcionalidades revisadas e integradas.

### Conferir a branch atual

```powershell
git branch --show-current
```

### Conferir alterações

```powershell
git status
```

### Atualizar a branch principal

```powershell
git switch main
git pull origin main
```

### Criar uma branch de desenvolvimento

```powershell
git switch -c feature/nome-da-funcionalidade
```

### Preparar um commit

```powershell
git add .
git commit -m "Descreve a funcionalidade implementada"
```

### Enviar a branch

```powershell
git push -u origin feature/nome-da-funcionalidade
```

Antes de enviar alterações, confira se não foram incluídos:

- Arquivos de banco de dados H2.
- Pastas de compilação, como `target`.
- Senhas ou tokens.
- Configurações de IP específicas do computador.
- Arquivos temporários.
- Cópias desnecessárias de projetos.

### Pull Request

Quando a funcionalidade estiver pronta:

1. Envie a branch ao GitHub.
2. Abra um Pull Request para a `main`.
3. Descreva as alterações realizadas.
4. Informe os testes executados.
5. Revise possíveis conflitos com alterações dos demais integrantes.
6. Integre somente após a revisão do grupo.

---

## 14. Problemas comuns

### A aplicação não inicia

Confira:

- Versão do Java.
- Configuração do `pom.xml`.
- Dependências do Maven.
- Mensagens apresentadas no terminal.
- Se a porta 8080 já está sendo utilizada.

### Funciona no computador, mas não no celular

Confira:

1. Se os dispositivos estão na mesma rede.
2. Se o endereço utilizado no celular contém o IPv4 correto.
3. Se o Spring Boot está em execução.
4. Se `server.address=0.0.0.0` está configurado.
5. Se o Firewall permite o acesso na rede local.
6. Se o Wi-Fi permite comunicação entre dispositivos.

### O QR Code abre um endereço localhost no celular

Confira a propriedade:

```properties
app.url-base=http://IP-DO-COMPUTADOR:8080
```

Reinicie o Spring Boot e gere uma nova nota para testar o QR Code com o endereço atualizado.

O endereço `localhost` no celular aponta para o próprio celular, não para o notebook.

### A câmera não abre dentro da página

A interface mobile orienta o usuário a utilizar o aplicativo de câmera do próprio celular para ler o QR Code.

A leitura ao vivo integrada ao navegador não faz parte desse fluxo do protótipo.

### O banco H2 não abre

Confira as propriedades do datasource e certifique-se de que a aplicação foi iniciada corretamente.

Verifique também se o console H2 está habilitado.

### O painel não atualiza imediatamente

A atualização automática ocorre periodicamente.

Também é possível utilizar a opção de atualização manual, quando disponível.

---

## 15. Segurança e limitações

O sistema foi criado para demonstração acadêmica em ambiente controlado.

Antes de qualquer utilização real, seriam necessários aprimoramentos como:

- Autenticação e autorização de operadores e clientes.
- Identificação segura do motorista.
- Integração com câmeras de reconhecimento de placas.
- Integração com sistemas fiscais oficiais.
- Integração com provedores reais de pagamento.
- Comunicação segura por HTTPS.
- Integração física com cancelas e sensores IoT.
- Auditoria, tratamento de dados e medidas de conformidade com a LGPD.

O protótipo não deve ser exposto diretamente à internet ou utilizado para processar pagamentos reais.

---

## 16. Situação do desenvolvimento

**Status: protótipo funcional em desenvolvimento e validação acadêmica.**

O sistema já possui interfaces web e mobile integradas, controle de entrada e saída, cálculo de tarifas, validação simulada de notas fiscais, pagamentos simulados, autorização temporária e renovação de saída.

O projeto permite demonstrar, em uma rede local, a comunicação entre o painel do operador e a página utilizada pelo motorista.

A validação final depende da execução e documentação dos cenários de teste previstos pelo grupo.

---

**Projeto acadêmico desenvolvido por estudantes do curso de Engenharia de Software do Centro Universitário UNA — Unidade Aimorés.**
