# TAGOX Flow — Arquitetura e Evolução do Sistema

**Produto:** TAGOX Flow
**Empresa responsável:** TAGOX Tech
**Repositório:** `Claytons1512FullStack/tagox-flow`
**Branch principal:** `main`
**Backend:** Java 21 + Spring Boot 4.1.1
**Banco de dados:** PostgreSQL 18.6
**Migrações:** Flyway
**Frontend planejado:** React + TypeScript
**Último commit de código publicado:** `f57e7f5` — `test: valida unicidade de email por tenant`
**Estado do Git antes desta atualização documental (14/10/2026):** branch `main` sincronizada com `origin/main`; diretório de trabalho limpo.
**Última validação completa confirmada (14/10/2026):** 71 testes aprovados, 0 falhas, 0 erros, 0 testes ignorados; BUILD SUCCESS.
**Data de atualização da documentação:** 14/10/2026
**Estado da documentação:** revisão do estado técnico atualizada em 14/10/2026; revisão integral do documento em andamento.

> **Regra de atualização:** este documento registra a arquitetura, as decisões tomadas, as funcionalidades implementadas e as pendências conhecidas. Uma funcionalidade só deve ser considerada concluída quando sua implementação e sua validação correspondente tiverem sido verificadas.

---

# 1. Visão Geral

O TAGOX Flow é uma plataforma SaaS desenvolvida pela TAGOX Tech para gestão de serviços profissionais.

O sistema está sendo construído com uma arquitetura modular, preparada para atender diferentes empresas e permitir a evolução progressiva de funcionalidades sem comprometer o isolamento dos dados.

O projeto utiliza Java e Spring Boot no backend, PostgreSQL como banco de dados relacional, Flyway para controle de alterações estruturais e autenticação baseada em JWT.

A primeira fundação do sistema contempla:

* Cadastro e gerenciamento de empresas contratantes.
* Estrutura multi-tenant.
* Cadastro de usuários.
* Associação de usuários a empresas.
* Controle de papéis e permissões por RBAC.
* Autenticação por JWT.
* Validação dos estados da empresa e do usuário durante o login.
* Onboarding técnico de uma nova empresa.
* Criação do primeiro usuário administrador.
* Migrações versionadas do banco de dados.
* Testes automatizados para os principais fluxos já desenvolvidos.

O objetivo atual é consolidar a fundação técnica e de segurança antes de avançar para os módulos de negócio.

---

# 2. Objetivo do Produto

O TAGOX Flow deverá permitir que empresas gerenciem seus processos e serviços em um ambiente centralizado, organizado e seguro.

A plataforma será desenvolvida para suportar diferentes contextos profissionais por meio de módulos e recursos reutilizáveis.

A primeira vertical planejada é a área de psicologia e saúde, sem limitar a arquitetura exclusivamente a esse segmento.

A evolução poderá contemplar outros segmentos, como:

* Clínicas e consultórios.
* Profissionais autônomos.
* Escritórios jurídicos.
* Empresas de serviços especializados.
* Outras organizações que necessitem de gestão operacional.

A arquitetura deve favorecer a reutilização de componentes, a separação de responsabilidades e a evolução independente dos módulos.

---

# 3. Modelo SaaS

O TAGOX Flow segue o modelo Software as a Service (SaaS).

Cada empresa cadastrada será representada por um `Tenant`, que delimita seu contexto organizacional e seus dados.

A estrutura deverá permitir que diferentes empresas utilizem a mesma aplicação sem acessar informações umas das outras.

O modelo comercial futuro poderá contemplar:

* Planos de assinatura.
* Contratos.
* Pagamentos.
* Módulos contratados.
* Limites de utilização.
* Permissões específicas por módulo.
* Ativação, suspensão e cancelamento de contratos.
* Regras comerciais por empresa.

**Importante:** a existência do papel `ADMIN` não significa que a empresa contratou todos os módulos da plataforma.

O controle de acesso por papel e o direito comercial de utilização de módulos são responsabilidades diferentes e deverão permanecer separados.

No estado atual, a fundação de multi-tenancy, usuários, papéis, autenticação e onboarding está implementada. O sistema completo de assinaturas, pagamentos, contratos e habilitação comercial de módulos permanece pendente.

---

# 4. Arquitetura Multi-Tenant

A arquitetura multi-tenant permite que várias empresas utilizem a mesma aplicação, mantendo seus dados isolados.

O modelo atual associa usuários a uma empresa por meio do relacionamento entre `User` e `Tenant`.

O JWT utilizado na autenticação contém os identificadores do usuário e do tenant.

O contexto de autenticação permite identificar:

* Qual usuário está autenticado.
* A qual empresa esse usuário pertence.
* Qual é o contexto de tenant da requisição autenticada.

O backend deve utilizar esse contexto para aplicar as regras de acesso aos dados.

## Regras obrigatórias

1. Um usuário pertence a um tenant.
2. Os dados de negócio devem respeitar o tenant proprietário.
3. A identificação do tenant não deve depender exclusivamente de um valor enviado pelo cliente.
4. O backend deve validar a autorização antes de permitir operações protegidas.
5. Consultas e operações de negócio devem ser limitadas ao contexto autorizado.
6. Um usuário de uma empresa não pode consultar ou alterar dados de outra empresa sem uma autorização administrativa explicitamente definida pela arquitetura.
7. Os testes devem verificar o isolamento entre tenants.

A implementação já possui a base de identificação do tenant por JWT e testes de isolamento registrados.

A proteção precisa continuar sendo aplicada aos novos serviços, repositórios, controllers e módulos adicionados ao sistema.

---

# 5. Stack Tecnológica

## Backend

* Java 21.
* Spring Boot 4.1.1.
* Spring Web.
* Spring Security.
* Spring Data JPA.
* Hibernate.
* Bean Validation.
* Autenticação JWT.
* BCrypt para hash de senhas.
* Maven.

## Banco de dados

* PostgreSQL 18.6.
* Flyway para versionamento do schema.
* Restrições relacionais e índices.
* Chaves primárias e estrangeiras.
* Restrições de unicidade.

## Frontend planejado

* React.
* TypeScript.
* Integração com a API REST.
* Gerenciamento de autenticação.
* Interfaces adaptadas aos papéis e permissões dos usuários.

O frontend ainda não deve ser considerado concluído.

## Ambiente de desenvolvimento

O desenvolvimento do backend é realizado com IntelliJ IDEA e Windows Git Bash, utilizando o Maven Wrapper quando apropriado.

---

# 6. Organização do Backend

O backend deve manter uma separação clara entre domínio, persistência, serviços, controllers, DTOs e configuração.

A organização segue os conceitos de arquitetura em camadas.

Estrutura conceitual:

```text
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── tagox/
│   │   │           └── flow/
│   │   │               ├── config/
│   │   │               ├── controller/
│   │   │               ├── domain/
│   │   │               ├── dto/
│   │   │               ├── exception/
│   │   │               ├── security/
│   │   │               └── service/
│   │   └── resources/
│   │       ├── db/
│   │       │   └── migration/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/
│               └── tagox/
│                   └── flow/
├── docs/
│   └── ARCHITECTURE.md
└── pom.xml
```
A estrutura acima representa a organização conceitual dos componentes. Os caminhos e pacotes concretos devem ser confirmados no código antes de qualquer reorganização.

## Responsabilidades

**Domain**

Representa entidades, enums e regras diretamente relacionadas ao domínio.

**Repository**

Responsável pelo acesso e pela persistência dos dados.

**Service**

Concentra as regras de negócio e coordena operações que envolvem múltiplas entidades.

**Controller**

Recebe requisições HTTP, valida entradas e delega a execução aos serviços.

**DTO**

Define os dados aceitos nas requisições e retornados pelas APIs.

**Security**

Concentra os componentes de autenticação, JWT e configuração de segurança.

**Exception**

Centraliza exceções de domínio e mecanismos de tratamento de erros.

A evolução deve preservar essa separação, evitando concentrar regras de negócio nos controllers.

---

# 7. Domínio Tenant

`Tenant` representa a empresa ou organização cadastrada na plataforma.

Cada tenant possui um contexto próprio dentro do sistema.

O domínio contempla os conceitos de:

* Identificador.
* Dados cadastrais.
* Documento.
* Slug.
* Status.
* Datas de criação e atualização.

Os campos concretos devem permanecer alinhados à entidade e às migrations existentes.

## Status do tenant

O enum `TenantStatus` contempla:

* `TRIAL`
* `ATIVO`
* `SUSPENSO`
* `CANCELADO`

## Significado dos estados

**TRIAL**

Empresa cadastrada em período ou condição de avaliação. O estado não representa, por si só, um sistema de cobrança ou expiração automática.

**ATIVO**

Empresa habilitada para utilizar as funcionalidades permitidas pelas regras atuais de autenticação e autorização.

**SUSPENSO**

Empresa cujo acesso deve ser bloqueado pelas regras implementadas.

**CANCELADO**

Empresa cujo acesso deve ser bloqueado pelas regras implementadas.

## Regras importantes

* O documento deve ser validado para evitar duplicidade.
* O slug deve ser validado para evitar conflitos.
* As regras de criação devem permanecer centralizadas no serviço correspondente.
* O onboarding utiliza o serviço de criação de tenant.
* A autenticação deve validar o estado do tenant.
* A mudança de estado deve respeitar as regras de negócio.

A expiração automática de um período de avaliação ainda não está implementada.

---

# 8. Domínio User

`User` representa uma pessoa com acesso ao TAGOX Flow.

O usuário está associado a um tenant e não deve ser tratado como um cadastro global independente da empresa.

O domínio contempla os conceitos de:

* Identificador.
* Nome.
* E-mail.
* Senha armazenada por meio de hash.
* Tenant associado.
* Status.
* Datas de criação e atualização.

Os campos definitivos são os presentes na entidade e no schema atual.

## Status do usuário

O enum de status do usuário contempla:

* `ATIVO`
* `INATIVO`
* `BLOQUEADO`

## Regras obrigatórias

1. O e-mail deve ser validado.
2. O usuário deve estar associado a um tenant.
3. A senha nunca deve ser armazenada em texto puro.
4. O hash da senha não deve ser retornado nas respostas da API.
5. Usuários inativos não devem conseguir autenticar.
6. Usuários bloqueados não devem conseguir autenticar.
7. A autenticação deve validar o estado do usuário antes de conceder acesso.
8. O sistema deve impedir a duplicidade de e-mail dentro do mesmo tenant.

A restrição de unicidade atual é composta por:

```text
(tenant_id, email)
```

Isso permite que empresas diferentes tenham usuários com o mesmo e-mail, caso essa seja a regra permitida pelo modelo, sem gerar duplicidade dentro da mesma empresa.

---

# 9. RBAC — Role-Based Access Control

O TAGOX Flow utiliza RBAC para organizar o acesso às funcionalidades por meio de papéis.

O objetivo é separar a identidade do usuário das permissões relacionadas às suas responsabilidades.

O modelo básico contempla:

* Usuário.
* Papel.
* Associação entre usuário e papel.
* Tenant ao qual o usuário pertence.

## Papéis padrão

* `ADMIN`
* `PROFISSIONAL`
* `ASSISTENTE`

## ADMIN

Representa o administrador da empresa no contexto do sistema.

O administrador poderá executar operações administrativas permitidas pela configuração de segurança e pelas regras de negócio.

Na implementação atual, a criação de usuários pela API protegida exige o papel `ADMIN`.

## PROFISSIONAL

Representa o usuário profissional.

Esse papel não concede automaticamente privilégios administrativos.

## ASSISTENTE

Representa o usuário assistente.

Esse papel também não concede automaticamente privilégios administrativos.

## Regra de segurança

A existência de um papel não deve ser interpretada como autorização irrestrita para acessar dados de outros tenants.

A autorização deve considerar a operação solicitada, o papel do usuário autenticado e o contexto da empresa.

O RBAC básico está implementado. A matriz completa de permissões para os futuros módulos ainda precisa ser definida.

---

# 10. Domínio Role

A entidade `Role` representa os papéis disponíveis na plataforma.

O enum `RoleType` contempla os papéis padrão:

```text
ADMIN
PROFISSIONAL
ASSISTENTE
```

A migration V6 é responsável pela inserção inicial dos papéis padrão no banco.

A aplicação deve utilizar os registros persistidos para associar papéis aos usuários.

## Regras

* Os papéis padrão devem existir para que as operações correspondentes funcionem.
* A criação de papéis padrão deve ser idempotente.
* Não se deve inserir duplicatas ao reaplicar uma operação equivalente.
* A associação deve utilizar o tipo de papel esperado.
* A ausência de um papel necessário deve gerar um erro controlado, sem deixar o onboarding parcialmente persistido.

A criação inicial dos papéis padrão está implementada por meio da migration V6.

---

# 11. UserRole — Associação entre Usuário e Papel

`UserRole` representa a associação entre um usuário e um papel.

Essa estrutura permite que a aplicação determine quais papéis estão atribuídos ao usuário autenticado.

A associação é utilizada na construção das regras de autorização.

## Responsabilidades

* Associar um usuário a um papel.
* Consultar os papéis atribuídos.
* Impedir associações duplicadas conforme as restrições do schema.
* Permitir que os serviços de negócio utilizem os papéis persistidos.
* Garantir que o usuário e a associação pertençam ao contexto correto.

No onboarding, o primeiro usuário criado recebe a associação ao papel `ADMIN`.

A criação do tenant, do usuário e da associação de papel deve fazer parte da mesma transação de onboarding.

Se alguma dessas operações falhar, a transação deve ser revertida.

---

# 12. Relacionamentos entre Entidades

Os principais relacionamentos do modelo atual são:

```text
Tenant
  └── User
       └── UserRole
            └── Role
```

## Tenant e User

Um tenant pode possuir vários usuários.

Cada usuário pertence a um tenant.

## User e Role

A associação entre usuário e papel é realizada por meio de `UserRole`.

O modelo permite atribuir papéis aos usuários sem incorporar toda a lógica de autorização diretamente à entidade `User`.

## Integridade

Os relacionamentos devem respeitar:

* Chaves estrangeiras.
* Identificadores válidos.
* Restrições de unicidade.
* Integridade transacional.
* Isolamento por tenant.
* Regras de exclusão e atualização definidas pelo schema.

Não devem ser realizadas alterações estruturais diretamente no banco sem uma migration correspondente.

---

# 13. Banco de Dados e Flyway

O PostgreSQL é o banco de dados utilizado pelo TAGOX Flow.

O Flyway controla a evolução do schema por meio de migrations versionadas.

A configuração do projeto utiliza validação do schema por meio de:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

O Hibernate deve validar a compatibilidade entre as entidades mapeadas e o schema, em vez de modificar automaticamente a estrutura do banco.

## Regras de evolução do banco

1. Alterações estruturais devem ser registradas em migrations.
2. Migrations já aplicadas não devem ser editadas retroativamente.
3. Novas mudanças devem receber novas versões.
4. As migrations devem ser testadas em um banco apropriado.
5. O schema deve permanecer compatível com as entidades JPA.
6. Restrições importantes de integridade devem ser mantidas no banco.
7. Dados necessários à inicialização devem ser tratados de maneira reproduzível.
8. A aplicação deve ser validada em ambiente de testes antes da evolução para produção.

As migrations V1 a V6 constituem o histórico estrutural registrado até o estado documentado neste arquivo.

---

# 14. Histórico das Migrations

## V1 — Criação do Tenant

Arquivo histórico correspondente à criação inicial da estrutura de tenant.

Estabelece a base cadastral das empresas.

## V2 — Atualização dos timestamps do Tenant

Adiciona ou ajusta os campos de datas relacionados ao ciclo de vida do tenant.

## V3 — Criação do usuário

Cria a estrutura inicial de persistência dos usuários.

## V4 — Criação dos papéis

Cria a estrutura necessária à persistência dos papéis utilizados pelo RBAC.

## V5 — Criação da associação UserRole

Cria a estrutura para associar usuários e papéis.

## V6 — Seed dos papéis padrão

Arquivo:

```text
V6__seed_standard_roles.sql
```

Responsável por inserir os papéis padrão:

```text
ADMIN
PROFISSIONAL
ASSISTENTE
```

A migration utiliza `gen_random_uuid()` para os identificadores e `ON CONFLICT (tipo) DO NOTHING` para evitar duplicação de registros cujo tipo já exista.

O comportamento é idempotente em relação à restrição de unicidade utilizada.

A função `gen_random_uuid()` foi verificada no ambiente utilizado durante o desenvolvimento.

## Regra de imutabilidade

**V1, V2, V3, V4, V5 e V6 não devem ser alteradas depois de aplicadas.**

Qualquer evolução estrutural futura deve ser implementada em uma nova migration, como V7 ou superior, de acordo com a necessidade real identificada.

O número da próxima migration deve ser confirmado no repositório antes de sua criação.

---

# 15. Integridade e Segurança dos Dados

A segurança do TAGOX Flow depende da combinação entre validação de entradas, regras de negócio, autenticação, autorização e integridade do banco.

## Senhas

As senhas devem ser processadas por meio do mecanismo de hash configurado, utilizando BCrypt.

O sistema não deve:

* Persistir senhas em texto puro.
* Retornar senhas nas respostas da API.
* Retornar hashes de senha nos DTOs públicos.
* Registrar senhas em logs.
* Expor credenciais em mensagens de erro.

## Dados cadastrais

Os documentos devem ser normalizados conforme as regras existentes no domínio.

O e-mail deve ser validado antes da persistência.

O slug do tenant deve respeitar as regras de unicidade.

## Transações

Operações que envolvam múltiplas entidades e dependências devem preservar a integridade transacional.

O onboarding é um exemplo importante: tenant, primeiro usuário e associação ao papel administrativo devem ser persistidos de maneira atômica.

## Segurança multi-tenant

Toda nova funcionalidade que manipule dados de negócio deve considerar o tenant autenticado.

A validação de papel, isoladamente, não substitui a verificação de propriedade dos dados.

## Segurança em produção

Ainda será necessário realizar uma revisão de produção abrangendo:

* Gerenciamento de segredos.
* Configuração dos ambientes.
* HTTPS.
* Política de expiração e renovação de tokens.
* Monitoramento e auditoria.
* Proteção contra abuso de endpoints públicos.
* Logs sem informações sensíveis.
* Backup e recuperação.
* Configuração de CORS, quando aplicável.
* Política de retenção de dados.
* Procedimentos de resposta a incidentes.

Esses pontos não devem ser considerados concluídos apenas porque a autenticação básica já funciona.

---

# 16. DTOs e Contratos da API

Os DTOs definem o contrato de comunicação entre clientes e backend.

Eles devem impedir que entidades internas sejam expostas diretamente de maneira descontrolada.

Os DTOs também permitem validar os dados recebidos antes da execução das regras de negócio.

## Regras

* Utilizar DTOs de entrada para operações que recebem dados externos.
* Validar campos obrigatórios.
* Validar formatos e limites aplicáveis.
* Não aceitar identificadores privilegiados sem a validação correspondente.
* Não expor hashes de senha.
* Não expor dados internos desnecessários.
* Manter respostas previsíveis.
* Evitar acoplar contratos públicos à estrutura interna das entidades.

## Autenticação

O login utiliza as informações necessárias para identificar o tenant e o usuário, além da senha.

O contrato registrado para o login contempla:

```text
tenantSlug
email
senha
```

## Criação de usuário

A criação de usuários utiliza o DTO correspondente ao contrato de criação, atualmente baseado nos campos:

```text
nome
email
senha
```

A implementação concreta deve permanecer alinhada aos DTOs presentes no repositório.

## Onboarding

O onboarding utiliza DTOs específicos para receber os dados necessários à criação do tenant e de seu primeiro administrador.

A resposta deve informar o resultado do cadastro sem retornar um JWT ou autenticar automaticamente o usuário.

---

# 17. Services e Regras de Negócio

Os serviços concentram as regras de negócio e coordenam operações entre entidades e repositórios.

Os componentes de serviço devem ser utilizados pelos controllers para evitar que a lógica de domínio fique concentrada nas camadas HTTP.

## TenantService

Responsável pelas operações de negócio relacionadas ao tenant, incluindo a criação conforme as regras existentes.

As validações de duplicidade de documento e slug devem permanecer centralizadas na lógica correspondente.

## UserService

Responsável pelas operações de negócio relacionadas aos usuários.

A criação de usuários deve respeitar a associação ao tenant, a validação dos dados e o armazenamento seguro da senha.

## UserRoleService

Responsável pelas associações entre usuários e papéis.

O onboarding utiliza esse serviço para atribuir o papel `ADMIN` ao primeiro usuário.

## AuthService

Responsável pelo fluxo de login e pela validação do tenant e do usuário.

O serviço verifica se os estados permitem a autenticação antes de emitir o token.

## OnboardingService

Responsável por coordenar a criação inicial de uma empresa e de seu administrador.

O fluxo utiliza a criação do tenant, a criação do usuário e a associação ao papel administrativo.

A operação deve ser transacional.

## Regra de arquitetura

Os controllers não devem duplicar as regras de negócio implementadas nos serviços.

Novos fluxos devem reutilizar os serviços existentes sempre que isso for compatível com suas responsabilidades.

---

# 18. Controllers e Endpoints da API

Os controllers recebem requisições HTTP e delegam a execução aos serviços.

Os endpoints implementados devem respeitar os contratos dos DTOs, a validação das entradas e as regras de segurança.

## Endpoints registrados

### Login

```http
POST /api/auth/login
```

Endpoint público utilizado para autenticar um usuário de um tenant.

O fluxo valida o tenant, o usuário e os estados correspondentes antes de emitir o JWT.

### Onboarding

```http
POST /api/onboarding
```

Endpoint público utilizado para cadastrar uma empresa e criar seu primeiro administrador.

O resultado esperado para uma criação bem-sucedida é HTTP `201 Created`.

O onboarding não emite JWT e não realiza login automático.

### Criação de tenant

```http
POST /api/tenants
```

O endpoint existente está protegido por autenticação.

Sua utilização não deve ser confundida com o onboarding público, que representa o fluxo de entrada de uma nova empresa.

### Criação de usuário

```http
POST /api/users
```

A criação de usuários pela API está protegida e exige o papel `ADMIN`, conforme a configuração atual.

## Evolução dos endpoints

Antes de adicionar novos endpoints, devem ser verificados:

* O contrato esperado.
* O serviço responsável.
* As permissões necessárias.
* A validação do tenant.
* O tratamento de exceções.
* Os testes de sucesso e falha.
* Os códigos HTTP.
* O impacto no isolamento de dados.

A lista acima documenta os endpoints relevantes registrados nesta etapa. Ela não representa necessariamente o inventário completo de todas as rotas existentes no código.

---

# 19. Autenticação e JWT

A autenticação do TAGOX Flow utiliza Spring Security e JWT.

O login é realizado pelo endpoint:

```http
POST /api/auth/login
```

O contrato de autenticação utiliza o slug do tenant, o e-mail e a senha do usuário.

## Fluxo de autenticação

1. O cliente envia os dados de login.
2. O backend identifica o tenant pelo slug informado.
3. O backend valida o estado do tenant.
4. O backend localiza o usuário no contexto do tenant.
5. O backend valida a senha.
6. O backend valida o estado do usuário.
7. Se todas as verificações forem aprovadas, o serviço gera o JWT.
8. O token contém os identificadores de usuário e tenant utilizados pela aplicação.

O token é gerado pelo componente `JwtService`, por meio do fluxo de autenticação existente.

## Validação do tenant

A autenticação permite tenants nos estados:

* `TRIAL`
* `ATIVO`

A autenticação deve ser recusada para tenants nos estados:

* `SUSPENSO`
* `CANCELADO`

## Validação do usuário

A autenticação deve ser recusada quando o usuário estiver:

* `INATIVO`
* `BLOQUEADO`

O usuário também precisa fornecer credenciais válidas.

## Decisão arquitetural

**O onboarding e o login são operações separadas.**

O onboarding cria a estrutura inicial da empresa e do administrador, mas não gera JWT e não autentica automaticamente o usuário.

Após concluir o cadastro, o usuário deve realizar o login pelo endpoint específico.

## Limites da implementação atual

A validação dos estados durante o login está implementada no estado local registrado.

Ainda devem ser avaliadas, conforme a evolução do sistema, as regras relacionadas a:

* Expiração do JWT.
* Renovação de tokens.
* Revogação de sessões.
* Alterações de estado ocorridas depois da emissão do token.
* Invalidade imediata de tokens em situações de bloqueio.
* Política de expiração de credenciais.

Não se deve presumir que uma alteração de estado no banco invalida automaticamente todos os JWTs já emitidos. Esse comportamento depende dos mecanismos de validação implementados em cada requisição.

---

# 20. Spring Security e Autorização

O backend utiliza Spring Security para proteger os endpoints.

A configuração existente contempla:

* Autenticação baseada em JWT.
* Aplicação stateless.
* Filtro de JWT.
* BCrypt.
* Segurança por método.
* Desativação de mecanismos de autenticação por formulário e HTTP Basic na configuração registrada.
* Proteção dos endpoints de acordo com as regras configuradas.

A configuração também deve tratar corretamente os endpoints públicos de login e onboarding.

## Endpoints públicos

Os fluxos abaixo devem poder ser acessados sem JWT:

```text
POST /api/auth/login
POST /api/onboarding
```

## Endpoints protegidos

Os demais endpoints devem respeitar a política de segurança configurada, salvo exceções públicas explicitamente definidas.

O endpoint de criação de tenant está protegido por autenticação.

A criação de usuários exige autorização administrativa conforme a configuração existente.

## Princípios

1. Autenticação e autorização são etapas distintas.
2. A existência de um JWT válido não concede acesso irrestrito.
3. A autorização deve ser verificada no backend.
4. Os papéis devem ser obtidos de acordo com a implementação de segurança existente.
5. O tenant autenticado deve ser respeitado nas operações de negócio.
6. Novos endpoints devem possuir uma política de acesso explícita.

A configuração deve continuar sendo revisada à medida que novos módulos forem adicionados.

---

# 21. Validação do RBAC e Isolamento entre Tenants

O RBAC foi validado por meio de testes de autorização HTTP.

O cenário registrado verifica a diferença de acesso entre o administrador e os demais papéis.

## Cenários registrados

| Papel        | Login    | Criação de usuário |
| ------------ | -------- | ------------------ |
| ADMIN        | HTTP 200 | HTTP 201           |
| PROFISSIONAL | HTTP 200 | HTTP 403           |
| ASSISTENTE   | HTTP 200 | HTTP 403           |

Os resultados acima correspondem aos cenários de validação registrados para os endpoints e as configurações atuais.

O login bem-sucedido de um profissional ou assistente não significa que esses usuários possuam autorização para operações administrativas.

## Isolamento de tenant

O sistema também possui testes registrados para validar o isolamento entre tenants.

O objetivo é impedir que a identidade de uma empresa seja utilizada para acessar dados pertencentes a outra.

A unicidade do e-mail é aplicada dentro de cada tenant: o mesmo endereço pode estar cadastrado em empresas diferentes, mas não pode ser duplicado entre usuários do mesmo tenant. Essa regra possui cobertura em testes automatizados.

## Continuidade da validação

A cada novo endpoint protegido, devem ser avaliados:

* Acesso com usuário ADMIN.
* Acesso com PROFISSIONAL.
* Acesso com ASSISTENTE.
* Acesso sem autenticação.
* Acesso com token inválido.
* Acesso com contexto de tenant diferente.
* Acesso a recursos pertencentes a outra empresa.

Os testes atuais validam a fundação de segurança já implementada. Eles não dispensam a criação de testes específicos para cada novo módulo.

---

# 22. Tratamento de Exceções e Erros

O backend deve retornar erros controlados, evitando expor detalhes internos da aplicação.

O tratamento de exceções deve diferenciar erros de validação, conflitos de negócio, falhas de autenticação e problemas internos.

## Categorias de erro

**Validação**

Dados de entrada inválidos ou incompletos.

**Conflito de negócio**

Tentativa de cadastrar informações incompatíveis com as regras, como dados únicos já existentes.

**Autenticação**

Credenciais inválidas ou usuário/tenant sem condições de autenticar.

**Autorização**

Usuário autenticado sem permissão para executar determinada operação.

**Erro interno**

Falha inesperada que não deve expor detalhes sensíveis ao cliente.

## Regras

* Não retornar stack traces ao cliente.
* Não expor credenciais ou hashes.
* Não revelar dados privados de outros tenants.
* Retornar códigos HTTP compatíveis com o resultado da operação.
* Registrar informações técnicas úteis para diagnóstico sem registrar segredos.
* Garantir que falhas transacionais não deixem cadastros incompletos.

No onboarding, falhas durante a criação do tenant, do usuário ou da associação de papel devem provocar rollback da transação.

---

# 23. Testes Automatizados e Validação Técnica

O projeto possui testes automatizados para a fundação de domínio, autenticação, autorização e onboarding.

A última execução completa confirmada em 14/10/2026 apresentou:

```text
71 testes executados
0 falhas
0 erros
0 testes ignorados
BUILD SUCCESS
```

**Histórico:** em 13/10/2026, a suíte registrou 58 testes aprovados. Em 14/10/2026, após a inclusão e validação de testes adicionais, a suíte completa passou a apresentar 71 testes aprovados.


## Categorias de testes registradas

* Testes de serviços.
* Testes de controllers.
* Testes de autenticação.
* Testes de isolamento entre tenants.
* Testes de autorização RBAC.
* Testes de onboarding.
* Testes de validação de requisições.
* Testes de persistência e comportamento transacional.

## Cenários de onboarding

Os cenários registrados incluem:

* Criação bem-sucedida de tenant.
* Tenant criado em `TRIAL`.
* Criação do primeiro usuário administrador.
* Associação do papel `ADMIN`.
* Ausência de JWT na resposta.
* Ausência de autenticação automática.
* Ausência de exposição de senha e hash.
* Rejeição de e-mail inválido.
* Rejeição de requisição inválida sem persistência dos registros esperados.
* Rollback quando o papel necessário não está disponível.

## Validação da autenticação

O fluxo de login possui validações para os estados do tenant e do usuário.

A revisão atual deve garantir que os testes cubram os estados permitidos e bloqueados, além das credenciais inválidas.

## Comandos de validação

Para executar a suíte completa no backend, o comando Maven Wrapper utilizado no ambiente é:

```bash
./mvnw test
```

Para executar uma classe específica, pode-se utilizar:

```bash
./mvnw -Dtest=NomeDaClasseTest test
```

Os comandos devem ser executados no diretório do backend.

Após mudanças no código, a suíte relevante deve ser executada novamente. Alterações exclusivamente documentais devem passar, no mínimo, por revisão do diff e validação de formatação; a execução dos testes pode ser repetida conforme o procedimento de qualidade do projeto.

---

# 24. Etapa 10 — RBAC

A Etapa 10 estabeleceu a fundação de controle de acesso por papéis.

O objetivo foi permitir que o sistema diferencie as responsabilidades de administradores, profissionais e assistentes.

## Entregas registradas

* Domínio de papéis.
* Papéis padrão.
* Associação entre usuário e papel.
* Persistência por meio do banco de dados.
* Integração com Spring Security.
* Autorização por papel nos endpoints protegidos.
* Validação dos acessos por HTTP.
* Testes de autorização.
* Fundamentos para aplicação de permissões aos módulos futuros.

## Critério de conclusão

A etapa de RBAC foi registrada como concluída.

Isso significa que a fundação necessária para distinguir os papéis está implementada e validada nos cenários existentes.

Não significa que todas as permissões futuras do produto já estejam definidas.

A matriz de permissões por módulo deverá ser ampliada à medida que novas funcionalidades forem desenvolvidas.

---

# 25. Etapa 10.1 — Onboarding Inicial do Tenant

A Etapa 10.1 implementa o fluxo técnico inicial de cadastro de uma nova empresa.

A implementação está concluída e publicada no repositório principal. A revisão final da documentação permanece pendente.

## Objetivo

Permitir que uma nova empresa entre no sistema por um endpoint público, criando sua estrutura inicial e o primeiro usuário administrador.

## Endpoint

```http
POST /api/onboarding
```

## Fluxo implementado

O fluxo utiliza os serviços existentes para:

1. Receber e validar os dados de onboarding.
2. Criar o tenant.
3. Definir o estado inicial do tenant como `TRIAL`.
4. Criar o primeiro usuário.
5. Definir o estado inicial do usuário como `ATIVO`.
6. Associar o papel `ADMIN` ao primeiro usuário.
7. Retornar uma resposta de onboarding sem JWT.
8. Finalizar a transação somente quando as operações necessárias forem concluídas com sucesso.

## Transação

A operação deve manter a consistência entre o tenant, o usuário e a associação ao papel.

Se uma etapa obrigatória falhar, a transação deve ser revertida.

Não deve existir um tenant parcialmente criado como consequência de uma falha na criação do primeiro administrador.

## Segurança

O endpoint é público porque precisa permitir que uma empresa ainda não cadastrada inicie o processo.

O fluxo não deve expor:

* Senha em texto puro.
* Hash de senha.
* JWT.
* Dados internos desnecessários.

## Onboarding não é login

O onboarding não gera JWT.

O onboarding não autentica automaticamente o primeiro administrador.

Após o cadastro, o usuário deve acessar:

```http
POST /api/auth/login
```

e fornecer as credenciais necessárias.

## Separação comercial

O onboarding técnico não realiza:

* Contratação de plano.
* Assinatura de contrato.
* Processamento de pagamento.
* Habilitação comercial de módulos.
* Definição definitiva de permissões comerciais.
* Ativação automática de todos os recursos.
* Expiração automática do período de avaliação.

Essas responsabilidades permanecem separadas do cadastro técnico.

## Validação

Em 13/10/2026, a suíte completa registrou 58 testes aprovados. Na execução completa mais recente, em 14/10/2026, foram registrados 71 testes aprovados, sem falhas, erros ou testes ignorados, com BUILD SUCCESS.

O estado local inclui a implementação do onboarding e a validação dos estados de tenant e usuário durante a autenticação.

A revisão final do documento e sua publicação ainda estão pendentes.

---

# 26. Decisões Arquiteturais Oficiais

As decisões abaixo orientam a evolução do TAGOX Flow.

## 26.1. Onboarding separado do login

O onboarding cria o tenant e seu primeiro administrador, mas não gera JWT.

O login permanece como operação independente.

## 26.2. Tenant inicial em TRIAL

O onboarding técnico cria o tenant no estado `TRIAL`.

Esse estado permite distinguir o cadastro inicial de uma empresa de uma ativação comercial definitiva.

## 26.3. Primeiro usuário como ADMIN

O primeiro usuário criado no onboarding recebe o papel `ADMIN`.

Esse papel é necessário para a administração inicial da empresa, respeitando as permissões implementadas.

## 26.4. Usuário inicial ativo

O primeiro usuário é criado no estado `ATIVO`.

Isso não significa que ele seja automaticamente autenticado. A autenticação continua dependendo do login.

## 26.5. Transação atômica

A criação do tenant, do usuário e da associação ao papel administrativo deve ocorrer dentro de uma única operação transacional.

## 26.6. Autenticação valida os estados

O login deve validar o estado do tenant e do usuário antes de emitir JWT.

## 26.7. TRIAL e ATIVO podem autenticar

A configuração atual permite autenticação de tenants em `TRIAL` e `ATIVO`.

Tenants `SUSPENSO` e `CANCELADO` devem ser bloqueados pelo fluxo de login.

Usuários `INATIVO` e `BLOQUEADO` também devem ser impedidos de autenticar.

## 26.8. RBAC e módulos comerciais são independentes

O papel de administrador não equivale à contratação de todos os módulos.

O controle de permissões técnicas e o controle de direitos comerciais devem ser separados.

## 26.9. Migrations aplicadas são imutáveis

V1 a V6 não devem ser editadas retroativamente.

Novas mudanças estruturais devem ser registradas em novas migrations.

## 26.10. Contratos comerciais serão tratados posteriormente

Assinaturas, pagamentos, planos, módulos contratados e ativação comercial definitiva não fazem parte do fluxo técnico atual de onboarding.

## 26.11. Documentação acompanha o estado real

A documentação deve distinguir claramente:

* Implementado.
* Testado.
* Em revisão.
* Pendente.
* Planejado.

Uma funcionalidade planejada não deve ser descrita como pronta para uso.

---

# 27. Funcionalidades Ainda Não Implementadas

As funcionalidades abaixo fazem parte da evolução prevista, mas não devem ser consideradas concluídas sem implementação e validação correspondentes.

## Gestão comercial

* Contratos.
* Assinaturas.
* Pagamentos.
* Planos comerciais.
* Renovação de assinaturas.
* Cancelamento de contratos.
* Inadimplência.
* Regras comerciais por tenant.

## Ciclo de vida do trial

* Data de expiração do período de avaliação.
* Campo `trial_ends_at`, caso aprovado no modelo definitivo.
* Processo automático de expiração.
* Notificações de término do período.
* Regras de transição do estado `TRIAL`.
* Bloqueio automático após expiração, caso definido pela regra comercial.

## Controle de módulos

* Cadastro de módulos.
* Associação de módulos contratados ao tenant.
* Habilitação e desabilitação de módulos.
* Verificação de acesso aos módulos contratados.
* Limites de utilização.
* Permissões detalhadas por funcionalidade.

## Gestão operacional

* Implementação completa dos módulos de negócio.
* APIs de cada módulo.
* Regras específicas por vertical.
* Auditoria detalhada das operações.
* Relatórios e indicadores.

## Frontend

* Aplicação React com TypeScript.
* Tela de onboarding.
* Tela de login.
* Gerenciamento seguro do estado de autenticação.
* Interface de administração.
* Interfaces específicas por papel.
* Integração com os endpoints do backend.

## Produção

* Configuração definitiva de ambientes.
* Observabilidade.
* Estratégia de backup e restauração.
* Pipeline de integração e entrega.
* Testes end-to-end.
* Homologação.
* Implantação em produção.

Essas pendências devem ser tratadas progressivamente, sem comprometer as regras de segurança já estabelecidas.

---

# 28. Primeira Vertical — Psicologia e Saúde

A primeira vertical planejada para o TAGOX Flow é a área de psicologia e saúde.

A arquitetura deve permitir desenvolver funcionalidades específicas desse segmento sem transformar as regras particulares de uma vertical em dependências obrigatórias de todo o sistema.

A implementação futura poderá exigir recursos como:

* Cadastro de profissionais.
* Cadastro de pacientes ou clientes.
* Agendamento.
* Gestão de atendimentos.
* Agenda e disponibilidade.
* Registro de informações operacionais.
* Relatórios.
* Controle de acesso por função.
* Auditoria de operações.

Esses itens representam possibilidades de evolução, não funcionalidades que devam ser consideradas concluídas no backend atual.

## Proteção de dados

Uma aplicação voltada à saúde exige cuidados adicionais com dados pessoais e informações sensíveis.

Antes de colocar esses módulos em produção, será necessário definir e validar:

* Quais dados serão coletados.
* Quem poderá acessar cada categoria de informação.
* Como os acessos serão registrados.
* Como serão tratados consentimento e outras bases legais aplicáveis.
* Como serão aplicadas as regras da LGPD.
* Como os dados serão protegidos durante o armazenamento e a transmissão.
* Quais serão as políticas de retenção e exclusão.
* Como serão realizadas cópias de segurança e restauração.

A arquitetura multi-tenant é uma base necessária, mas não substitui essas medidas.

---

# 29. Roadmap Oficial de Evolução

A evolução do TAGOX Flow deve seguir uma sequência que preserve a integridade do sistema.

## Etapa A — Fundação técnica

**Estado:** implementada na base atual, com necessidade de continuidade das validações.

Abrange:

* Estrutura inicial do backend.
* Domínio Tenant.
* Domínio User.
* Persistência.
* Migrations iniciais.
* Validação do schema.

## Etapa B — Autenticação

**Estado:** implementada na base atual.

Abrange:

* Spring Security.
* BCrypt.
* JWT.
* Login.
* Identificação de usuário e tenant.
* Validação de estado de tenant e usuário no login.

## Etapa C — RBAC

**Estado:** fundação implementada e validada.

Abrange:

* Papéis padrão.
* Associação UserRole.
* Integração com segurança.
* Proteção de endpoints.
* Testes de autorização.

## Etapa D — Onboarding inicial

**Estado:** implementação concluída no estado local registrado; revisão e publicação documental pendentes.

Abrange:

* Endpoint público de onboarding.
* Criação de tenant em TRIAL.
* Criação do primeiro usuário em ATIVO.
* Associação do papel ADMIN.
* Transação atômica.
* Resposta sem JWT.
* Login separado.
* Testes automatizados.

## Etapa E — Revisão e consolidação

**Estado:** próxima atividade imediata.

Abrange:

* Conferência da documentação.
* Preservação das 45 seções.
* Verificação de consistência com o código.
* Revisão do diff.
* Validação final apropriada.
* Commit documental.
* Publicação após revisão.

## Etapa F — Módulos de negócio

**Estado:** pendente.

Abrange a implementação progressiva das funcionalidades da primeira vertical e das estruturas reutilizáveis.

## Etapa G — Contratos e módulos comerciais

**Estado:** pendente.

Abrange planos, contratos, assinaturas, pagamentos e regras de habilitação comercial.

## Etapa H — Frontend

**Estado:** pendente.

Abrange a aplicação React com TypeScript e a integração com o backend.

## Etapa I — Homologação e produção

**Estado:** pendente.

Abrange testes integrados, revisão de segurança, observabilidade, infraestrutura e implantação.

---

# 30. Critérios de Prontidão para Produção

A fundação atual do backend não deve ser confundida com a prontidão do produto completo para produção.

Antes da implantação comercial, deverão ser avaliados os seguintes critérios.

## Segurança

* Revisão completa dos endpoints públicos e protegidos.
* Verificação do ciclo de vida dos JWTs.
* Proteção de segredos.
* Revisão das permissões.
* Testes de isolamento entre tenants.
* Política de CORS adequada ao ambiente.
* HTTPS.
* Controle de abuso dos endpoints públicos.
* Tratamento seguro de erros.

## Banco de dados

* Migrations testadas.
* Estratégia de backup.
* Procedimento de restauração validado.
* Restrições de integridade revisadas.
* Estratégia de migração entre versões.
* Configuração adequada de conexões.
* Monitoramento de falhas.

## Aplicação

* Configuração separada por ambiente.
* Logs adequados.
* Métricas e monitoramento.
* Tratamento de falhas.
* Testes automatizados.
* Testes integrados.
* Procedimentos de recuperação.

## Produto

* Módulos de negócio implementados.
* Regras comerciais definidas.
* Fluxos do usuário validados.
* Frontend integrado.
* Homologação concluída.
* Documentação operacional disponível.

## Operação

* Processo de implantação.
* Procedimento de rollback.
* Monitoramento após implantação.
* Responsáveis pela operação definidos.
* Procedimentos para incidentes.
* Política de atualização e manutenção.

A prontidão para produção deverá ser declarada somente depois que esses requisitos forem avaliados e validados conforme o escopo de lançamento.

---

# 31. Critérios de Qualidade por Etapa

Cada etapa deve ter critérios de conclusão verificáveis.

## Domínio

* Entidades coerentes.
* Regras de negócio centralizadas.
* Estados válidos.
* Relacionamentos consistentes.
* Ausência de duplicação desnecessária.

## Persistência

* Migrations versionadas.
* Restrições de integridade.
* Schema compatível com JPA.
* Estratégia transacional correta.
* Ausência de alterações manuais não documentadas.

## Autenticação

* Credenciais validadas.
* Senhas armazenadas por hash.
* JWT gerado somente após autenticação válida.
* Tenant validado.
* Usuário validado.
* Estados bloqueados tratados corretamente.

## Autorização

* Endpoints protegidos.
* Papéis verificados no backend.
* Isolamento de tenant.
* Testes de acesso autorizado e negado.

## Onboarding

* Tenant criado.
* Usuário administrador criado.
* Papel ADMIN associado.
* Transação atômica.
* Ausência de JWT na resposta.
* Login separado.
* Falhas sem persistência parcial.

## Testes

* Casos positivos.
* Casos negativos.
* Validações de entrada.
* Cenários de segurança.
* Testes transacionais.
* Testes de isolamento.

## Documentação

* Correspondência com o código.
* Histórico de decisões.
* Pendências explícitas.
* Migrations identificadas corretamente.
* Histórico de validação atualizado.
* Estado do Git descrito sem ambiguidades.

Uma etapa só deve ser declarada concluída quando os critérios pertinentes tiverem sido verificados.

---

# 32. Estado Atual do Projeto

Em 14/10/2026, o TAGOX Flow possui a fundação técnica para autenticação, autorização inicial e onboarding de uma nova empresa, com 71 testes aprovados na última execução completa.

## Componentes implementados

* Estrutura base do backend Java.
* Spring Boot.
* Persistência com PostgreSQL.
* Flyway.
* Domínio Tenant.
* Domínio User.
* Domínio Role.
* Associação UserRole.
* Papéis padrão.
* Autenticação JWT.
* Hash de senhas com BCrypt.
* Configuração de Spring Security.
* Validação do estado do tenant durante o login.
* Validação do estado do usuário durante o login.
* RBAC básico.
* Proteção de endpoints administrativos.
* Identificação do tenant por JWT.
* Fundamentos de isolamento entre tenants.
* Onboarding público.
* Criação do primeiro administrador.
* Transação do fluxo de onboarding.
* Testes automatizados dos cenários implementados.

## Última validação completa confirmada em 14/10/2026

```text
Testes: 71
Falhas: 0
Erros: 0
Ignorados: 0
Build: SUCCESS
```

A validação completa confirmada em 14/10/2026, após a inclusão de testes adicionais, concluiu com 71 testes aprovados, sem falhas, erros ou testes ignorados; BUILD SUCCESS.

## Pendências atuais

* Revisão final integral da documentação.
* Confirmação de que todas as 45 seções estão presentes.
* Revisão do diff de `docs/ARCHITECTURE.md`.
* Verificação de que não houve alterações inesperadas no código.
* Validação final apropriada.
* Commit da documentação.
* Push após revisão.
* Desenvolvimento dos módulos de negócio.
* Frontend.
* Contratos e assinaturas.
* Homologação e preparação para produção.

O projeto não deve ser considerado concluído integralmente. A fundação técnica está avançada, enquanto os módulos comerciais e operacionais permanecem em evolução.

---

# 33. Histórico do Git e Publicação

O repositório utilizado é:

```text
Claytons1512FullStack/tagox-flow
```

A branch principal é `main`.

## Último commit publicado confirmado

```text
f57e7f5 test: valida unicidade de email por tenant
```

## Histórico recente confirmado em 14/10/2026

```text
f57e7f5 test: valida unicidade de email por tenant
69b8f0c test: valida cadastro e associacao de roles
cbe7c08 feat: implementa módulo de pacientes
fb2178d docs: atualiza arquitetura e estado do TAGOX Flow
445f73e fix: validate user and tenant status during authentication
697ef24 feat: implementa onboarding inicial do tenant
19c0a70 feat: habilita autenticacao de tenant em trial
a6daa3c docs: atualiza arquitetura e onboarding
```

Os commits acima foram conferidos com `git log -8 --oneline --decorate` em 14/10/2026. Os registros anteriores a `f57e7f5` permanecem no histórico do repositório.

## Estado do Git confirmado em 14/10/2026

Na última verificação, `main` e `origin/main` apontavam para `f57e7f5`, sem divergência entre as referências. A situação do diretório de trabalho deve ser verificada novamente antes do commit documental, pois esta revisão modifica o arquivo de arquitetura.

## Registro histórico do trabalho documental

Em uma etapa anterior, a documentação foi registrada como modificada, com a branch local um commit à frente de `origin/main`. Esse estado é histórico e não representa a situação confirmada em 14/10/2026.

## Regra de publicação

Não executar `git push` sem revisar o conteúdo que será publicado.

Antes de qualquer commit documental, confirmar:

1. Todas as 45 seções estão presentes.
2. Os dados técnicos estão coerentes.
3. As decisões arquiteturais foram preservadas.
4. As funcionalidades futuras continuam identificadas como pendentes.
5. Nenhum arquivo de código foi alterado inesperadamente.
6. O diff documental foi revisado.
7. A validação apropriada foi concluída.

---

# 34. Próximas Ações Imediatas

A próxima atividade é consolidar a documentação sem modificar o comportamento do backend.

## Passo 1 — Revisar a estrutura documental

Confirmar a presença das seções 1 a 45.

Verificar se a numeração é contínua e se nenhum assunto relevante foi removido.

## Passo 2 — Revisar o conteúdo

Conferir especialmente:

* Autenticação e JWT.
* Estados de tenant e usuário.
* RBAC.
* Migration V6.
* Onboarding.
* Transações.
* Testes.
* Histórico do Git.
* Estado atual do projeto.
* Pendências comerciais.
* Critérios de produção.

## Passo 3 — Conferir o diff

Revisar as alterações de `docs/ARCHITECTURE.md`.

Confirmar que as alterações se limitam ao escopo documental pretendido.

## Passo 4 — Conferir o estado do repositório

Verificar branch, commits e arquivos modificados.

Não presumir que o estado atual é igual ao último snapshot registrado.

## Passo 5 — Validar a documentação

Executar a verificação de formatação e revisar o documento integralmente.

Como o trabalho desta etapa é documental, não se deve afirmar que o código foi revalidado sem executar os testes correspondentes.

## Passo 6 — Criar o commit documental

Somente após a revisão, criar um commit com uma mensagem coerente com o conteúdo efetivamente alterado.

Exemplo de mensagem possível:

```text
docs: atualiza arquitetura e estado do TAGOX Flow
```

A mensagem é uma sugestão; deve corresponder ao diff final.

## Passo 7 — Revisar e publicar

Conferir o novo histórico e publicar as alterações somente após a revisão final.

**Não misturar essa atividade com mudanças adicionais de código.**

---

# 35. Visão de Longo Prazo

O TAGOX Flow deverá evoluir para uma plataforma SaaS modular, capaz de atender diferentes empresas com segurança e flexibilidade.

A evolução deve preservar a separação entre:

* Identidade.
* Empresa.
* Autenticação.
* Autorização.
* Contrato comercial.
* Módulos contratados.
* Dados operacionais.
* Auditoria.

## Evolução esperada

A plataforma poderá incorporar:

* Múltiplas verticais de negócio.
* Módulos ativados por empresa.
* Regras de permissão específicas.
* Gestão de contratos.
* Assinaturas e pagamentos.
* Administração centralizada.
* Relatórios.
* Auditoria.
* Integrações externas.
* Frontend integrado.
* Monitoramento operacional.
* Escalabilidade de infraestrutura.

A arquitetura deverá permitir que esses componentes sejam adicionados sem enfraquecer o isolamento multi-tenant.

## Princípio de evolução

A prioridade é construir uma fundação confiável antes de ampliar a quantidade de funcionalidades.

A expansão do produto deve ocorrer por etapas verificáveis, mantendo o código, os testes e a documentação alinhados.

---

# 36. Considerações Técnicas Finais

O TAGOX Flow está sendo desenvolvido de forma incremental.

As decisões arquiteturais registradas até aqui devem orientar a implementação das próximas etapas.

Os princípios essenciais são:

1. Isolamento entre tenants.
2. Autenticação segura.
3. Autorização explícita.
4. Separação entre onboarding e login.
5. Transações consistentes.
6. Migrations versionadas.
7. DTOs para contratos externos.
8. Regras de negócio centralizadas.
9. Testes automatizados.
10. Documentação alinhada ao código.
11. Separação entre RBAC e contratação comercial.
12. Validação antes da publicação.

Nenhuma nova funcionalidade deve comprometer esses princípios sem uma decisão arquitetural explícita e documentada.

---

# 37. Homologação e Deploy

A homologação e a implantação são etapas futuras do TAGOX Flow.

A fundação técnica atual não significa que o produto completo esteja pronto para implantação comercial.

## Homologação

Antes de uma implantação, deverão ser validados:

* Fluxo de onboarding.
* Fluxo de login.
* Validação dos estados de tenant.
* Validação dos estados de usuário.
* RBAC.
* Isolamento entre empresas.
* Persistência.
* Migrations.
* Tratamento de erros.
* Integração entre frontend e backend.
* Regras dos módulos de negócio.
* Cenários de falha e recuperação.

## Ambientes

A estratégia definitiva deverá distinguir, no mínimo, os ambientes de desenvolvimento, testes e produção conforme as necessidades do projeto.

Cada ambiente deverá utilizar configuração e credenciais apropriadas.

Os dados de teste não devem ser confundidos com dados de produção.

## Deploy

O processo de implantação deverá definir:

* Como a aplicação será construída.
* Como as variáveis de ambiente serão configuradas.
* Como o banco será preparado.
* Como as migrations serão aplicadas.
* Como a saúde da aplicação será verificada.
* Como será feito o rollback.
* Como serão monitorados erros após a implantação.

**Estado atual:** homologação integrada e deploy de produção permanecem pendentes.

---

# 38. Critério de Prontidão para Produção

A prontidão para produção deverá ser determinada por evidências verificáveis, e não apenas pela conclusão de uma etapa de desenvolvimento.

## Critérios obrigatórios

### Segurança

* Autenticação revisada.
* Autorização revisada.
* Isolamento entre tenants testado.
* Senhas protegidas.
* Segredos fora do código-fonte.
* Endpoints públicos revisados.
* JWT e sua política de ciclo de vida revisados.

### Banco

* Migrations validadas.
* Integridade referencial verificada.
* Backups configurados.
* Restauração testada.
* Conexões configuradas adequadamente.

### Aplicação

* Testes automatizados aprovados.
* Integração validada.
* Tratamento de erros revisado.
* Logs e monitoramento configurados.
* Configuração de produção revisada.

### Produto

* Módulos previstos para o lançamento concluídos.
* Frontend integrado, quando fizer parte do lançamento.
* Fluxos principais homologados.
* Regras comerciais definidas.
* Documentação operacional disponível.

### Operação

* Deploy reproduzível.
* Plano de rollback.
* Monitoramento pós-implantação.
* Procedimentos para incidentes.
* Responsabilidades operacionais definidas.

## Estado atual

A fundação de autenticação, RBAC e onboarding possui validações registradas.

Isso não comprova que todos os requisitos de produção estejam satisfeitos.

A declaração de prontidão deverá ocorrer após uma avaliação específica do escopo real de lançamento.

---

# 39. Critério de Qualidade por Etapa

Cada etapa do TAGOX Flow deve ter um critério objetivo de conclusão.

## Critério de implementação

A funcionalidade deve estar implementada no código correspondente.

## Critério de integração

Os componentes envolvidos devem funcionar em conjunto, respeitando os contratos estabelecidos.

## Critério de segurança

As permissões, as entradas e o isolamento de dados devem ser verificados nos cenários aplicáveis.

## Critério de testes

Os testes relevantes devem ser executados, e os resultados devem ser registrados com precisão.

## Critério documental

A documentação deve refletir o comportamento real da implementação.

## Critério de publicação

As alterações devem ser revisadas antes de serem incluídas no histórico compartilhado.

## Aplicação ao onboarding

O onboarding foi implementado com:

* Endpoint público.
* Criação de tenant.
* Criação do primeiro usuário.
* Associação do papel ADMIN.
* Transação.
* Resposta sem JWT.
* Login independente.
* Testes automatizados registrados.

Em 13/10/2026, a suíte completa registrou 58 testes aprovados. Na execução completa mais recente, em 14/10/2026, foram registrados 71 testes aprovados, sem falhas, erros ou testes ignorados, com BUILD SUCCESS.

A revisão e publicação da documentação permanecem pendentes.

## Regra geral

Não marcar uma funcionalidade como concluída apenas porque o código foi escrito.

A conclusão deve considerar implementação, integração, segurança, testes e documentação pertinentes ao escopo.

---

# 40. Registro Histórico do Estado do Projeto em 09/10/2026

Esta seção registra o estado conhecido em 09/10/2026.

## Repositório

```text
Projeto: TAGOX Flow
Repositório: Claytons1512FullStack/tagox-flow
Branch: main
Backend: backend/
Documentação: docs/ARCHITECTURE.md
```

## Histórico registrado

```text
Último commit local:
445f73e fix: validate user and tenant status during authentication

Último commit publicado:
697ef24 feat: implementa onboarding inicial do tenant
```

O estado registrado indicava que a branch local estava um commit à frente da branch remota.

## Código

A fundação técnica contempla:

* Tenant.
* User.
* Role.
* UserRole.
* PostgreSQL.
* Flyway V1–V6.
* JWT.
* BCrypt.
* Spring Security.
* RBAC básico.
* Validação dos estados de tenant e usuário durante o login.
* Onboarding técnico.
* Criação do primeiro administrador.
* Testes de isolamento e autorização.

## Banco

As migrations V1 a V6 fazem parte do histórico conhecido.

A migration V6 insere os papéis padrão.

As migrations aplicadas devem permanecer imutáveis.

## Testes

Última execução completa registrada:

```text
58 testes aprovados
0 falhas
0 erros
0 ignorados
BUILD SUCCESS
```

## Documentação

A documentação foi modificada para atualização.

A verificação `git diff --check` não apresentou erros no estado registrado.

O diff é extenso e exige revisão de conteúdo e estrutura.

## Estado de conclusão

**Implementado:** fundação técnica, autenticação, RBAC básico, onboarding e validações registradas.

**Pendente:** revisão documental final, commit e publicação da documentação, além das etapas futuras de produto.

Este estado é um registro histórico. Antes de executar novas operações, o estado atual do Git e dos testes deve ser confirmado.

---

# 41. Último Estado Validado — 13/10/2026

O último estado técnico validado registrado para esta atualização apresenta os seguintes resultados.

## Testes automatizados

```text
Total: 58
Falhas: 0
Erros: 0
Ignorados: 0
Resultado: BUILD SUCCESS
```

A validação foi executada em 13/10/2026, após as alterações no backend registradas no commit 445f73e.


## Autenticação

O fluxo de login valida os estados do tenant e do usuário antes da emissão do JWT.

Os tenants em `TRIAL` e `ATIVO` podem autenticar conforme a regra implementada.

Os tenants em `SUSPENSO` e `CANCELADO` devem ser bloqueados.

Os usuários em `INATIVO` e `BLOQUEADO` devem ser bloqueados.

## RBAC

Os cenários HTTP registrados demonstram que o papel ADMIN pode criar usuários, enquanto PROFISSIONAL e ASSISTENTE recebem HTTP 403 nessa operação.

## Onboarding

O fluxo cria o tenant, o primeiro usuário e a associação ADMIN.

A operação é transacional e não deve emitir JWT nem autenticar automaticamente o usuário.

## Git

O último estado registrado apontava:

* Branch local `main`.
* Um commit local à frente de `origin/main`.
* `docs/ARCHITECTURE.md` modificado.
* `git diff --check` sem erros.

## Limite desta validação

Os resultados acima descrevem o último estado registrado.

A revisão documental final e a publicação não devem ser consideradas concluídas até que sejam efetivamente verificadas.

---

# 42. Próxima Ação

A próxima ação oficial é finalizar a documentação sem alterar o comportamento do código.

## Ordem de execução

1. Conferir a existência das 45 seções.
2. Verificar a numeração e a continuidade dos assuntos.
3. Revisar as informações técnicas.
4. Confirmar as decisões do onboarding.
5. Conferir o histórico de migrations.
6. Conferir os commits registrados.
7. Revisar integralmente o diff.
8. Confirmar que não existem alterações inesperadas no código.
9. Executar as validações apropriadas.
10. Criar o commit documental.
11. Revisar o novo histórico.
12. Publicar as alterações somente após a revisão.

## Restrições

* Não alterar migrations já aplicadas.
* Não implementar funcionalidades futuras dentro desta etapa documental.
* Não modificar autenticação ou autorização sem uma necessidade identificada.
* Registrar a data e o resultado de cada execução de testes somente após sua confirmação.
* Não publicar alterações sem revisar o diff.
* Não remover seções da documentação para simplificar o arquivo.

## Resultado esperado

Um documento completo, coerente com o código e com as decisões tomadas, mantendo as 45 seções e distinguindo claramente o estado implementado das etapas futuras.

---

# 43. Visão de Longo Prazo

O objetivo de longo prazo é transformar o TAGOX Flow em uma plataforma SaaS modular, segura e preparada para diferentes segmentos profissionais.

A plataforma deverá permitir que empresas utilizem recursos compatíveis com seus contratos e que os usuários acessem apenas as funcionalidades autorizadas.

## Pilares

### Multi-tenancy

Isolamento dos dados por empresa.

### Segurança

Autenticação, autorização, controle de acesso e proteção dos dados.

### Modularidade

Módulos de negócio com responsabilidades bem definidas.

### Evolução comercial

Contratos, assinaturas, pagamentos e habilitação de módulos.

### Escalabilidade

Arquitetura preparada para crescer sem perder a separação de responsabilidades.

### Observabilidade

Capacidade de identificar falhas e acompanhar o comportamento do sistema.

### Manutenibilidade

Código organizado, testes automatizados e documentação atualizada.

## Direção arquitetural

A expansão deverá ocorrer por etapas, utilizando a fundação técnica atual e evitando a implementação prematura de funcionalidades comerciais sem regras definidas.

O crescimento do sistema deverá preservar a integridade dos dados e o isolamento entre empresas.

---

# 44. Considerações Finais

O TAGOX Flow está em desenvolvimento incremental.

A fundação de tenant, usuário, papéis, autenticação, autorização inicial e onboarding representa uma etapa importante da arquitetura.

As decisões estabelecidas até aqui devem ser mantidas durante a evolução:

* O onboarding cria o tenant e o primeiro administrador.
* O tenant inicia em `TRIAL`.
* O usuário inicial inicia em `ATIVO`.
* O primeiro usuário recebe o papel `ADMIN`.
* O onboarding não gera JWT.
* O login permanece separado.
* A autenticação valida os estados do tenant e do usuário.
* O RBAC não substitui o isolamento de tenant.
* A contratação comercial permanece separada da criação técnica da empresa.
* As migrations aplicadas são imutáveis.
* A documentação deve refletir o código real.
* A publicação deve ocorrer somente após revisão.

O projeto não deve ser tratado como concluído em sua totalidade. As funcionalidades comerciais, os módulos de negócio, o frontend e a preparação para produção permanecem etapas futuras.

A prioridade imediata é consolidar a documentação, revisar o estado do repositório e publicar as alterações de forma controlada.

---

# 45. Registro Oficial da Atualização de 09/10/2026

Esta seção preserva o registro histórico consolidado em 09/10/2026. As informações referentes à execução dos testes em 13/10/2026 foram acrescentadas posteriormente como atualização complementar, sem alterar a data ou o conteúdo histórico das decisões originalmente registradas.

## 45.1. Validação de estados na autenticação

Foi registrado o commit local:

```text
445f73e fix: validate user and tenant status during authentication
```

A alteração contempla a validação dos estados do tenant e do usuário durante o login.

A autenticação permite tenants em `TRIAL` e `ATIVO` e bloqueia tenants em `SUSPENSO` e `CANCELADO`.

Usuários `INATIVO` e `BLOQUEADO` também devem ter a autenticação recusada.

## 45.2. Onboarding técnico

O onboarding inicial está implementado no estado local registrado.

O endpoint público:

```http
POST /api/onboarding
```

cria o tenant, o primeiro usuário e a associação ao papel `ADMIN`.

A operação é transacional e não gera JWT.

O login continua sendo realizado separadamente.

## 45.3. Papéis padrão

A migration V6 registrada é:

```text
V6__seed_standard_roles.sql
```

Ela insere os papéis padrão:

```text
ADMIN
PROFISSIONAL
ASSISTENTE
```

As migrations V1 a V6 devem permanecer imutáveis.

## 45.4. Testes

A última execução completa registrada apresentou:

```text
58 testes
0 falhas
0 erros
0 ignorados
BUILD SUCCESS
```

Este resultado pertence ao registro histórico de 09/10/2026. Em 13/10/2026, os testes foram executados novamente: 58 testes, 0 falhas, 0 erros, 0 ignorados e BUILD SUCCESS.

## 45.5. Histórico do Git

Último commit local registrado:

```text
445f73e
```

Último commit publicado registrado:

```text
697ef24
```

O estado anterior à conclusão da revisão documental indicava a branch local um commit à frente de `origin/main`, com `docs/ARCHITECTURE.md` modificado.

## 45.6. Decisões preservadas

Permanecem válidas as decisões de:

* Separar onboarding e login.
* Não emitir JWT durante o onboarding.
* Criar o tenant em TRIAL.
* Criar o primeiro usuário em ATIVO.
* Associar o papel ADMIN ao primeiro usuário.
* Utilizar transação para evitar persistência parcial.
* Validar estados durante a autenticação.
* Separar RBAC das regras comerciais.
* Manter imutáveis as migrations aplicadas.

## 45.7. Pendências

Permanecem pendentes:

* Revisão final da documentação.
* Conferência integral das 45 seções.
* Revisão do diff.
* Verificação do estado atual do Git.
* Commit documental.
* Publicação após revisão.
* Desenvolvimento dos módulos de negócio.
* Gestão de contratos, planos e pagamentos.
* Frontend.
* Homologação e preparação para produção.

## 45.8. Estado oficial

**No estado conhecido em 09/10/2026, o TAGOX Flow possuía a fundação de autenticação, RBAC básico e onboarding técnico implementada, com 58 testes aprovados na validação registrada naquela data.**

**A documentação ainda precisa passar pela revisão final e pelo processo de publicação.**

Este registro consolida o estado conhecido em 09/10/2026 e deverá ser atualizado quando novas alterações forem implementadas e validadas.
