TAGOX Flow — Arquitetura e Evolução do Sistema

Produto: TAGOX Flow
Empresa: TAGOX Tech
Repositório: Claytons1512FullStack/tagox-flow
Branch principal: main
Último commit: 0c0f031 feat: adiciona seed dos roles padrão
Estado: Etapa 10 concluída e publicada; Etapa 10.1 — Fundação de Onboarding em andamento; roles padrão já garantidas por Flyway.
1. Visão Geral

O TAGOX Flow é uma plataforma SaaS desenvolvida pela TAGOX Tech para gestão de serviços profissionais.

A plataforma está sendo construída com uma arquitetura modular e multi-tenant, preparada para atender diferentes verticais de serviços profissionais.

A primeira vertical considerada para evolução da plataforma é a área de psicologia e saúde, mantendo uma base arquitetural reutilizável para futuras verticais, como medicina, jurídico e outros serviços profissionais.

O projeto está sendo desenvolvido de forma incremental, priorizando:

fundação arquitetural;
arquitetura multi-tenant;
isolamento de dados;
gerenciamento de usuários;
autenticação;
autorização;
RBAC;
integridade do banco de dados;
versionamento através de Flyway;
testes automatizados;
segurança;
escalabilidade;
preparação para produção.

O princípio adotado é construir primeiro um Core SaaS sólido, antes de acoplar os módulos funcionais específicos de cada vertical.

2. Objetivo do TAGOX Flow

O objetivo do TAGOX Flow é fornecer uma plataforma centralizada para gestão de serviços profissionais, permitindo que diferentes organizações utilizem o mesmo sistema com segurança e isolamento de dados.

Cada organização cadastrada na plataforma representa um Tenant.

A arquitetura deve garantir:

cada Tenant possui seus próprios usuários e dados;
usuários possuem vínculo obrigatório com um Tenant;
dados entre organizações permanecem isolados;
usuários são autenticados;
acessos são controlados por roles e regras de autorização;
a plataforma pode evoluir para diferentes segmentos sem comprometer sua base.

O TAGOX Flow é concebido como um produto SaaS da TAGOX Tech.

3. Modelo SaaS

O TAGOX Flow utiliza o conceito de Software as a Service (SaaS).

Uma única aplicação atende múltiplas organizações clientes.

Cada organização possui uma representação lógica própria dentro da plataforma, denominada Tenant.

O modelo adotado é:

Shared Database + Shared Schema + isolamento lógico por tenant_id.

Diferentes organizações utilizam a mesma infraestrutura, porém os dados permanecem separados através das relações de Tenant existentes no domínio e das regras de aplicação.

A regra fundamental da arquitetura é:

Nenhum Tenant pode acessar ou manipular dados pertencentes a outro Tenant.

4. Arquitetura Multi-Tenant
4.1 Estratégia adotada

O TAGOX Flow utiliza:

Shared Database + Shared Schema + tenant_id

A aplicação utiliza uma estrutura compartilhada de banco de dados para múltiplas organizações.

O isolamento ocorre através do relacionamento entre as entidades e o Tenant responsável pelos registros.

Estrutura fundamental atual:

Tenant
 └── User
      └── UserRole
           └── Role

Os futuros módulos de negócio deverão respeitar o mesmo princípio de associação e isolamento.

4.2 Conceito de Tenant

Tenant representa uma organização cliente dentro da plataforma.

Exemplos de organizações que poderão utilizar o TAGOX Flow:

clínicas;
consultórios;
escritórios;
empresas de serviços profissionais.

Cada Tenant possui seus próprios usuários e dados operacionais.

Estados atualmente definidos:

TRIAL;
ATIVO;
SUSPENSO;
CANCELADO.
Regra arquitetural definida

O estado TRIAL será considerado um estado funcional da plataforma e deverá permitir autenticação.

A política comercial de duração, limites e expiração do Trial será definida posteriormente.

4.3 Isolamento de Dados

O isolamento entre organizações é um requisito fundamental da arquitetura.

Na implementação atual, o usuário possui uma referência obrigatória para seu Tenant através do campo:

tenant_id

Um usuário pertencente a um Tenant não deve acessar informações pertencentes a outro Tenant.

Essa regra já possui cobertura através de testes automatizados e validação HTTP real.

4.4 Integridade Multi-Tenant

A integridade da associação entre usuários e Tenants é protegida pelo banco e pela aplicação.

Regras atuais:

usuário obrigatoriamente pertence a um Tenant;
relacionamento protegido por chave estrangeira;
não existe usuário normal sem organização vinculada;
duplicidade de usuário é controlada por Tenant e email;
o Tenant utilizado na criação de usuários autenticados é obtido do contexto do usuário autenticado;
o cliente não pode escolher arbitrariamente o tenantId no corpo da requisição.

A restrição atual permite:

(tenant_id, email)

Isso significa:

Tenant A
mesmo@email.com

Tenant B
mesmo@email.com

podem coexistir.

Dentro do mesmo Tenant, o email não pode ser duplicado.

5. Stack Tecnológica

O TAGOX Flow está sendo desenvolvido utilizando uma stack baseada em Java e Spring Boot.

Backend

Tecnologias principais:

Java 21;
Spring Boot 4.1.1;
Spring Web MVC;
Spring Data JPA;
Hibernate;
Spring Security;
Bean Validation;
JWT;
BCrypt;
Maven.
Banco de Dados
PostgreSQL 18.6.
Migração de Banco
Flyway.

Todas as alterações estruturais do banco devem ser realizadas através de migrations versionadas.

Testes

O projeto utiliza:

Spring Boot Test;
JUnit;
testes de persistência;
testes de serviços;
testes de controllers;
testes de isolamento multi-tenant;
testes de autenticação;
testes de JWT;
testes de autorização/RBAC.
6. Estrutura do Backend

O backend segue uma organização baseada em domínio, separando responsabilidades entre entidades, serviços, controladores, DTOs, segurança e tratamento de exceções.

Estrutura principal:

controller
domain
dto
service
security
config
exception
Controller

Responsável pelos endpoints HTTP.

Domain

Contém entidades, enums e repositórios relacionados ao domínio.

DTO

Contém objetos utilizados na comunicação entre API e aplicação.

Service

Contém regras de negócio e operações de aplicação.

Security

Contém os componentes relacionados à autenticação e contexto de segurança.

Config

Contém configurações da aplicação, incluindo Spring Security.

Exception

Centraliza exceções de negócio e tratamento global de erros.

7. Domínio Tenant

Tenant representa a organização cliente dentro da plataforma.

Responsabilidades:

identificar a organização;
manter informações próprias da empresa;
servir como base de isolamento dos dados;
controlar o ciclo de vida da organização.

A implementação atual possui:

Tenant;
TenantRepository;
TenantService;
TenantController;
TenantStatus;
TipoPessoa;
DTOs de Tenant.
8. Domínio User

User representa um usuário pertencente a uma organização dentro do TAGOX Flow.

Cada usuário possui:

identificação própria;
Tenant associado;
nome;
email;
senha armazenada através de hash;
status;
informações relacionadas à persistência.

A associação obrigatória com Tenant garante que usuários estejam sempre vinculados a uma organização.

A implementação atual possui:

User;
UserRepository;
UserStatus;
UserService;
UserController;
CreateUserRequest;
UserResponse.
9. RBAC — Controle de Acesso

O TAGOX Flow utiliza RBAC — Role-Based Access Control — como modelo inicial de autorização.

O objetivo é permitir que usuários possuam diferentes níveis de acesso conforme sua função dentro da organização.

O modelo implementado é:

User
 ↓
UserRole
 ↓
Role

Os papéis atualmente definidos são:

ADMIN;
PROFISSIONAL;
ASSISTENTE.

O RBAC já está integrado ao Spring Security.

10. Domínio Role

Role representa um papel de acesso dentro do TAGOX Flow.

A implementação possui:

Role;
RoleRepository;
RoleType.

Os tipos atualmente definidos são:

ADMIN
PROFISSIONAL
ASSISTENTE

A entidade Role possui:

identificador UUID;
tipo;
descrição.

O tipo é persistido como enumeração textual.

Próxima evolução

As roles padrão deverão ser garantidas pelo Flyway.

Será criada uma nova migration, sem alterar V1–V5:

V6__seed_default_roles.sql

Essa migration será responsável por garantir as roles padrão do sistema.

11. Domínio UserRole

UserRole representa a associação entre um usuário e um papel de acesso.

A relação é:

User → UserRole → Role

A implementação utiliza chave composta através de:

UserRole;
UserRoleId;
UserRoleRepository.

O relacionamento permite que um usuário possua uma ou mais roles.

12. Relacionamentos do Modelo

O modelo atual possui os seguintes relacionamentos principais:

Tenant
 └── User
      └── UserRole
           └── Role

Conceitualmente:

Tenant possui vários Users;
User pertence obrigatoriamente a um Tenant;
User pode possuir múltiplas associações UserRole;
Role pode estar associada a múltiplos usuários.

Os futuros módulos de negócio deverão seguir o mesmo princípio de isolamento.

13. Banco de Dados e Flyway

O TAGOX Flow utiliza PostgreSQL como banco de dados principal.

A evolução estrutural do banco é controlada pelo Flyway.

O Hibernate está configurado para validar o schema existente:

spring.jpa.hibernate.ddl-auto=validate

Isso garante que:

Hibernate não cria tabelas automaticamente;
alterações estruturais devem ser feitas através de migrations;
o modelo Java deve permanecer alinhado ao banco.

Regra:

Migrations aplicadas não devem ser alteradas.

Novas alterações devem ser implementadas através de novas versões.

14. Histórico das Migrations

Atualmente existem seis migrations principais.

V1 — create_tenant

Criação inicial da estrutura de Tenant.

V2 — alter_tenant_timestamps

Ajustes relacionados aos timestamps do Tenant.

V3 — create_usuario

Criação da tabela de usuários.

Inclui:

vínculo com Tenant;
dados cadastrais;
status;
timestamps.
V4 — create_role

Criação da estrutura de papéis.

Inclui:

identificação;
tipo;
descrição.
V5 — create_usuario_role

Criação da associação entre usuários e papéis.

Utiliza chave composta:

(usuario_id, role_id)

V6 — seed_standard_roles

Garante as roles padrão do sistema:

ADMIN;
PROFISSIONAL;
ASSISTENTE.

A migration foi aplicada com sucesso e utiliza inserção idempotente através de:

ON CONFLICT (tipo) DO NOTHING.

15. Integridade e Segurança

As regras de integridade são protegidas em diferentes camadas.

Banco de Dados
UUID;
chaves primárias;
chaves estrangeiras;
constraints;
campos obrigatórios;
unicidade.
Aplicação
validação de entrada;
regras de negócio;
Services;
tratamento global de exceções.
Segurança
autenticação;
JWT;
BCrypt;
Spring Security;
autorização por role;
isolamento por Tenant.
16. DTOs

O TAGOX Flow utiliza DTOs para controlar a comunicação entre API e aplicação.

Objetivos:

separar entrada e saída das entidades;
evitar exposição direta das entidades JPA;
controlar dados recebidos;
facilitar validações;
manter contratos da API mais estáveis.
16.1 DTOs de Tenant

Incluem:

TenantRequestDTO;
TenantResponseDTO.
16.2 DTOs de User

Incluem:

CreateUserRequest;
UserResponse.

Uma decisão importante já implementada:

CreateUserRequest não recebe mais tenantId para determinar o Tenant do usuário em uma operação autenticada.

O Tenant é determinado pelo contexto autenticado.

17. Services

A camada de Services concentra as regras de negócio.

Responsabilidades:

operações de domínio;
validações;
fluxos de aplicação;
controle transacional quando necessário;
comunicação com repositories.

Principais Services atuais:

TenantService;
UserService;
UserRoleService;
AuthService.
17.1 TenantService

Responsável por operações relacionadas ao Tenant.

Inclui:

criação;
validação;
regras de negócio;
consulta;
persistência através do repository.
17.2 UserService

Responsável pelas operações relacionadas aos usuários.

Inclui:

criação;
validação;
associação ao Tenant;
proteção contra duplicidade;
persistência da senha através de hash.

No fluxo autenticado, o Tenant utilizado pelo UserService vem do contexto autenticado.

17.3 UserRoleService

Responsável pela associação entre usuários e roles.

Inclui:

busca do usuário;
busca da role;
verificação de duplicidade;
criação de UserRole.
17.4 AuthService

Responsável pelo fluxo de autenticação.

O login utiliza:

tenantSlug
email
senha

Fluxo:

Login
 ↓
Tenant
 ↓
User
 ↓
Validação de status
 ↓
BCrypt
 ↓
JWT

O JWT contém:

userId
tenantId
Evolução definida

O TRIAL deverá ser aceito como estado autenticável.

A regra futura será:

TRIAL      → login permitido
ATIVO      → login permitido
SUSPENSO   → login bloqueado
CANCELADO  → login bloqueado

A política de duração e expiração do Trial ainda não será implementada nesta etapa.

18. Controllers

Os Controllers representam a camada HTTP.

Responsabilidades:

receber requisições;
validar DTOs;
encaminhar para Services;
retornar respostas HTTP.

Regras complexas de negócio devem permanecer nos Services.

Fluxo:

Cliente HTTP
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
Banco
18.1 TenantController

Responsável pelos endpoints relacionados aos Tenants.

18.2 UserController

Responsável pelos endpoints relacionados aos usuários.

A criação de usuário está protegida por autorização:

@PreAuthorize("hasRole('ADMIN')")

Portanto, atualmente:

ADMIN        → pode criar usuário
PROFISSIONAL → não pode criar usuário
ASSISTENTE    → não pode criar usuário
19. Autenticação e Segurança

A camada de autenticação já está implementada.

O TAGOX Flow utiliza:

Spring Security;
JWT;
BCrypt;
autenticação stateless;
JwtAuthenticationFilter;
AuthenticatedUser.
19.1 Fluxo de Autenticação
POST /api/auth/login
        ↓
AuthService
        ↓
validação Tenant
        ↓
validação User
        ↓
BCrypt
        ↓
JwtService
        ↓
JWT
19.2 JWT

O JWT contém:

userId
tenantId

O JwtAuthenticationFilter extrai essas informações.

Em seguida, busca as roles do usuário.

Fluxo:

JWT
 ↓
userId + tenantId
 ↓
UserRoleRepository
 ↓
RoleType
 ↓
GrantedAuthority
 ↓
Spring Security

As authorities são geradas como:

ROLE_ADMIN
ROLE_PROFISSIONAL
ROLE_ASSISTENTE
20. Autorização RBAC

A autorização é aplicada através do Spring Security.

Foi habilitado:

@EnableMethodSecurity

Exemplo atual:

@PreAuthorize("hasRole('ADMIN')")

O fluxo completo é:

Request
 ↓
JWT
 ↓
JwtAuthenticationFilter
 ↓
roles
 ↓
GrantedAuthority
 ↓
Spring Security
 ↓
@PreAuthorize
 ↓
Endpoint permitido ou 403
21. Validação Real do RBAC

O RBAC foi validado com o servidor real em execução.

ADMIN

Login:

HTTP 200

Criação de usuário:

HTTP 201
PROFISSIONAL

Login:

HTTP 200

Criação de usuário:

HTTP 403
ASSISTENTE

Login:

HTTP 200

Criação de usuário:

HTTP 403

Isso comprovou o funcionamento da cadeia completa:

JWT → Roles → Authorities → Authorization
22. Tratamento de Exceções

O TAGOX Flow possui uma camada dedicada para tratamento de erros.

Componentes:

GlobalExceptionHandler;
BusinessException;
DuplicateResourceException;
ResourceNotFoundException.

Objetivos:

padronizar respostas;
centralizar tratamento;
separar regras de negócio de HTTP;
facilitar manutenção.
23. Testes Automatizados

O projeto possui testes automatizados para validar a estabilidade do Core.

A última execução registrada apresentou:

Tests run: 48
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS

Data da execução:

2026-09-26 23:45:44 -03:00
23.1 Testes de Tenant

Cobrem:

criação;
persistência;
regras;
endpoints;
validações.
23.2 Testes de User

Cobrem:

criação;
validação;
persistência;
associação ao Tenant;
duplicidade.
23.3 Testes Multi-Tenant

UserTenantIsolationTest valida isolamento entre organizações.

Também existem testes HTTP demonstrando que Tenants diferentes podem possuir o mesmo email sem compartilhamento de dados.

23.4 Testes de Authentication

A autenticação possui testes para:

login;
JWT;
serviço de autenticação;
controller de autenticação.
23.5 Testes de RBAC

A autorização possui testes para:

ADMIN autorizado;
PROFISSIONAL bloqueado;
ASSISTENTE bloqueado;
carregamento das roles;
HTTP 403.
24. Etapa 10 — Autorização RBAC
Status: CONCLUÍDA

A Etapa 10 foi finalizada, auditada, testada e publicada.

Commit:

749f565 feat: implementa autorizacao RBAC

Push:

main → origin/main

Working tree ficou limpa antes do push.

Essa etapa estabeleceu a autorização real da plataforma.

25. Etapa 10.1 — Fundação de Onboarding
Status: DEFINIDA — PRÓXIMA ETAPA

Antes dos módulos de negócio, será concluída a fundação de onboarding.

Objetivos:

10.1.1 — Roles padrão

Status: CONCLUÍDA

A migration Flyway V6 foi criada para garantir as roles padrão:

ADMIN
PROFISSIONAL
ASSISTENTE

A V6 foi aplicada com sucesso sem alterar as migrations anteriores.

10.1.2 — TRIAL funcional

Permitir autenticação de Tenants em:

TRIAL

Mantendo:

ATIVO      → login permitido
SUSPENSO   → login bloqueado
CANCELADO  → login bloqueado

10.1.3 — Primeiro ADMIN

O onboarding deverá garantir que um novo Tenant não fique sem administrador.

Modelo:

Criar Tenant
↓
TRIAL
↓
Criar primeiro User
↓
ATIVO
↓
ADMIN
↓
Criar UserRole
↓
Tenant pronto

Essas operações deverão ocorrer dentro de uma única transação.

A orquestração será responsabilidade de uma camada de aplicação própria de onboarding, utilizando os Services existentes sempre que possível.

Em caso de falha em qualquer etapa:

ROLLBACK

O usuário ADMIN criado durante o onboarding não será autenticado automaticamente. O acesso ocorrerá posteriormente pelo fluxo normal de login.

10.1.4 — Endpoint de onboarding

O onboarding será exposto através de um endpoint público separado:

POST /api/onboarding

O endpoint será responsável por orquestrar a criação inicial da organização:

Tenant
↓
User
↓
ADMIN
↓
UserRole

O Tenant será criado inicialmente com status TRIAL.

O primeiro usuário será criado como ATIVO e receberá a role ADMIN.

Todas as operações deverão ocorrer dentro de uma única transação.

Em caso de falha em qualquer etapa:

ROLLBACK

O onboarding não irá gerar JWT nem autenticar automaticamente o usuário.

Após a conclusão do onboarding, a autenticação continuará sendo realizada através do fluxo normal:

POST /api/auth/login

A regra de negócio deverá existir em uma camada de aplicação reutilizável, permitindo futuramente diferentes formas de entrada, como onboarding self-service ou onboarding controlado pela TAGOX, sem duplicação das regras de domínio.

26. Decisão sobre Trial

Foi decidido que:

TRIAL é um estado funcional do Tenant e permite login.

Não serão implementados agora:

duração fixa de 14 dias;
trial_ends_at;
job de expiração;
limite de usuários;
limite de recursos;
cobrança;
assinatura;
suspensão automática.

Essas regras serão definidas quando o modelo comercial do SaaS estiver formalizado.

27. Decisões Arquiteturais

27.1 Multi-Tenant

Modelo:

Shared Database
+
Shared Schema
+
tenant_id
27.2 UUID

Entidades principais utilizam UUID.

Motivos:

menor exposição de IDs sequenciais;
integração;
geração distribuída;
melhor adequação a APIs públicas.
27.3 Flyway

Flyway é a fonte oficial de evolução estrutural do banco.

Migrations aplicadas não devem ser modificadas.

27.4 DTOs

Entidades JPA não devem ser utilizadas diretamente como contratos da API quando houver necessidade de DTO específico.

27.5 RBAC

Modelo inicial:

User
↓
UserRole
↓
Role
27.6 Segurança Multi-Tenant

O tenantId não deve ser escolhido pelo cliente em operações autenticadas.

O Tenant deve ser determinado pelo contexto confiável da autenticação.

27.7 Onboarding

O primeiro usuário de um novo Tenant deverá ser ADMIN.

Tenant + primeiro ADMIN devem ser criados atomicamente.

O onboarding técnico não representa contratação comercial.

O fluxo de contratação será tratado separadamente e poderá envolver:

seleção de módulos;
proposta comercial;
contrato;
assinatura;
pagamento;
confirmação da contratação pela TAGOX Tech;
configuração do Tenant;
ativação dos módulos contratados;
definição das permissões;
criação ou ativação dos usuários;
envio das credenciais ou instruções de acesso.

A existência da role ADMIN não significa que o Tenant possui acesso automático a todos os módulos do sistema.

Roles representam o que um usuário pode fazer.

Módulos contratados representam o que o Tenant possui contratado.

Essas duas dimensões deverão permanecer separadas na arquitetura.

28. Funcionalidades Ainda Não Implementadas

Após a conclusão do Core atual, ainda fazem parte do roadmap:

Onboarding
onboarding completo;
primeiro ADMIN;
fluxo público/controlado;
regras de Trial.
Permissões avançadas
permissões por recurso;
permissões por módulo;
controle granular.
Módulos de negócio
clientes/pacientes;
agenda;
atendimentos;
prontuário;
documentos;
financeiro;
relatórios;
configurações.

Os módulos exatos serão definidos antes da implementação da primeira vertical.

29. Primeira Vertical — Psicologia e Saúde

A primeira vertical planejada para evolução comercial do TAGOX Flow é psicologia e saúde.

A vertical utilizará o Core já desenvolvido:

Tenant
User
Role
RBAC
Authentication
Authorization
Multi-Tenant

Os módulos específicos serão construídos sobre essa fundação.

A arquitetura deverá permanecer reutilizável para futuras verticais.

30. Roadmap Oficial
Fase 1 — Fundação SaaS
Etapa 1 — Tenant

✅ Concluída

Etapa 2 — User Base

✅ Concluída

Etapa 3 — Exceptions

✅ Concluída

Etapa 4 — PostgreSQL + Flyway

✅ Concluída

Etapa 5 — JWT

✅ Concluída

Etapa 6 — Authentication

✅ Concluída

Etapa 7 — RBAC Base

✅ Concluída

Etapa 8 — Tenant via JWT

✅ Concluída

Etapa 9 — Isolamento HTTP

✅ Concluída

Etapa 10 — Autorização RBAC

✅ Concluída

Etapa 10.1 — Fundação de Onboarding

🔜 Próxima

31. Fase 2 — Módulos de Negócio
Etapa 11 — Módulo de negócio inicial

🔜

Processo obrigatório:

AUDITAR
 ↓
DEFINIR
 ↓
MODELAR
 ↓
MIGRATION
 ↓
IMPLEMENTAR
 ↓
TESTAR
 ↓
VALIDAR ISOLAMENTO
 ↓
REGISTRAR

Nenhum módulo será criado sem definição clara de suas regras de negócio.

32. Etapa 12 — API Completa

Após os módulos principais:

endpoints;
DTOs;
validações;
paginação;
filtros;
autorização;
tratamento de erros;
isolamento;
documentação.
33. Etapa 13 — Frontend

Frontend:

React
+
TypeScript

O frontend será desenvolvido depois que o Core da API estiver suficientemente estável.

Principais fluxos:

Login
 ↓
Dashboard
 ↓
Tenant
 ↓
Usuários
 ↓
Roles
 ↓
Módulos
34. Etapa 14 — Integração Frontend + Backend

Serão integrados:

autenticação;
JWT;
chamadas API;
tratamento de 401;
tratamento de 403;
Tenant;
usuários;
roles;
módulos.
35. Etapa 15 — E2E

Serão testados fluxos completos:

Onboarding
 ↓
Login
 ↓
Dashboard
 ↓
CRUD
 ↓
Autorização
 ↓
Isolamento
 ↓
Logout

Também serão executados cenários negativos.

Exemplo:

Tenant A
   X
Tenant B
36. Etapa 16 — Segurança e Preparação para Produção

Antes do lançamento serão revisados:

JWT secret;
variáveis de ambiente;
credenciais;
CORS;
CSRF conforme arquitetura final;
headers de segurança;
rate limiting;
logs;
tratamento de exceções;
exposição de informações;
permissões;
PostgreSQL;
migrations;
backups;
recuperação de desastre;
HTTPS;
observabilidade;
auditoria;
configurações específicas de produção.

Durante o desenvolvimento já foi identificado o aviso de senha de segurança gerada pelo Spring Boot.

Esse comportamento será eliminado ou devidamente configurado antes da produção.

37. Etapa 17 — Homologação e Deploy

O lançamento não será feito diretamente do ambiente de desenvolvimento.

O fluxo planejado será:

Desenvolvimento
      ↓
Testes automatizados
      ↓
Integração
      ↓
E2E
      ↓
Revisão de segurança
      ↓
Homologação
      ↓
Validação final
      ↓
Produção
38. Critério de Prontidão para Produção

O TAGOX Flow não será considerado pronto apenas porque a aplicação inicia ou porque os testes unitários passam.

Antes do lançamento, deverá existir:

Core
+
Multi-Tenant
+
Authentication
+
Authorization
+
Onboarding
+
Business Modules
+
API
+
Frontend
+
Integration
+
E2E
+
Security
+
Observability
+
Backup
+
Production Configuration
+
Deployment

Todos os componentes críticos deverão estar validados.

39. Critério de Qualidade por Etapa

O desenvolvimento seguirá o ciclo:

AUDITAR
 ↓
DECIDIR
 ↓
IMPLEMENTAR
 ↓
TESTAR
 ↓
VALIDAR
 ↓
REGISTRAR
 ↓
AVANÇAR

Não avançaremos simplesmente porque uma funcionalidade "parece funcionar".

Cada etapa deverá possuir:

implementação;
testes;
validação;
decisão documentada;
commit;
push quando apropriado.
40. Estado Atual Exato do Projeto

Neste momento:

┌────────────────────────────────────────────┐
│             TAGOX FLOW BACKEND             │
├────────────────────────────────────────────┤
│ Tenant                         ✅           │
│ User Base                      ✅           │
│ Exceptions                     ✅           │
│ PostgreSQL                     ✅           │
│ Flyway V1–V6                   ✅           │
│ JWT                            ✅           │
│ Authentication                 ✅           │
│ RBAC Base                      ✅           │
│ Tenant via JWT                 ✅           │
│ HTTP Isolation                 ✅           │
│ Authorization RBAC             ✅           │
│ Testes automatizados           ✅           │
│ Validação HTTP real            ✅           │
│ Onboarding Foundation          🔜           │
│ Business Modules               🔜           │
│ Complete API                   🔜           │
│ React + TypeScript             🔜           │
│ Frontend/Backend Integration   🔜           │
│ E2E                            🔜           │
│ Production Security            🔜           │
│ Homologation                   🔜           │
│ Deploy                         🔜           │
└────────────────────────────────────────────┘
41. Último Estado Validado

Última execução da suíte:

Tests run: 48
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS

Último commit:

0c0f031 feat: adiciona seed dos roles padrão

Branch:

main

O commit foi publicado no GitHub.

42. Próxima Ação

A próxima atividade oficial é:

Etapa 10.1.2 — Validar o comportamento de autenticação do Tenant em TRIAL.

A sequência planejada é:

Validar TRIAL no AuthService
↓
Criar/ajustar testes de autenticação
↓
Validar login de Tenant TRIAL
↓
Modelar DTOs do onboarding
↓
Implementar OnboardingService
↓
Implementar POST /api/onboarding
↓
Criar Tenant + primeiro User ADMIN em transação única
↓
Testar rollback
↓
Testar isolamento e autorização
↓
Validar fluxo completo
↓
Atualizar documentação
↓
Commit
↓
Push
↓
Concluir Etapa 10.1
↓
Etapa 11

A V6 já foi criada e publicada.

Não serão alteradas as migrations V1–V6.

43. Visão de Longo Prazo

O TAGOX Flow não está sendo construído como uma aplicação isolada para uma única empresa.

A arquitetura está sendo preparada como uma plataforma SaaS da TAGOX Tech.

O Core deverá permitir:

TAGOX Flow Core
       │
       ├── Psicologia / Saúde
       │
       ├── Medicina
       │
       ├── Jurídico
       │
       └── Outras verticais

Cada vertical poderá possuir seus próprios módulos e regras específicas, mantendo a fundação comum de:

Tenants;
usuários;
autenticação;
autorização;
RBAC;
segurança;
isolamento;
infraestrutura;
auditoria;
integrações.
44. Considerações Finais

O TAGOX Flow já possui uma fundação importante do produto.

As etapas iniciais estabeleceram:

arquitetura multi-tenant;
persistência;
Flyway;
usuários;
JWT;
autenticação;
RBAC;
autorização;
isolamento entre Tenants;
testes automatizados;
validação HTTP real.

A Etapa 10 consolidou a autorização real através do Spring Security.

A Etapa 10.1 está em andamento.

A primeira inconsistência identificada, relacionada à dependência de inserção manual das roles padrão, foi resolvida através da migration V6.

O próximo objetivo é concluir a Fundação de Onboarding, validando o comportamento de autenticação do Tenant em TRIAL e implementando o fluxo transacional de criação inicial:

Tenant
↓
TRIAL
↓
primeiro User
↓
ATIVO
↓
ADMIN
↓
UserRole

Após a conclusão do onboarding, o projeto estará preparado para entrar na construção dos módulos de negócio.

A evolução continuará seguindo uma regra fundamental:

Primeiro construir uma fundação segura e consistente; depois construir funcionalidades sobre ela.

O objetivo final é entregar um TAGOX Flow que não apenas funcione em desenvolvimento, mas possua as condições técnicas necessárias para evoluir até um produto SaaS real, seguro, multi-tenant, modular e preparado para produção.

Estado oficial do documento

Última atualização: 27/09/2026
Etapa concluída: 10 — Autorização RBAC
Etapa 10.1: Fundação de Onboarding em andamento
Última subetapa concluída: 10.1.1 — Roles padrão
Próxima subetapa: 10.1.2 — Validação do TRIAL
Último commit: 0c0f031 feat: adiciona seed dos roles padrão
Testes registrados: 48/48 passando
Status do projeto: Core de segurança concluído; V6 publicada; fundação de onboarding em andamento.
