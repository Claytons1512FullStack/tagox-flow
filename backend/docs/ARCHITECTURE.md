# TAGOX Flow — Arquitetura e Evolução do Sistema

## 1. Visão Geral

O TAGOX Flow é uma plataforma SaaS desenvolvida pela TAGOX Tech para gestão de serviços profissionais.

A plataforma está sendo construída com uma arquitetura modular, preparada para atender diferentes verticais de serviços profissionais.

A primeira vertical considerada para evolução da plataforma é a área de psicologia e saúde, mantendo uma base arquitetural reutilizável para futuras verticais, como medicina e jurídico.

O projeto está sendo desenvolvido de forma incremental, priorizando inicialmente:

- fundação arquitetural;
- arquitetura multi-tenant;
- isolamento de dados;
- gerenciamento de usuários;
- controle de acesso;
- integridade do banco de dados;
- testes automatizados;
- segurança e escalabilidade.

Neste estágio, a prioridade é construir uma base sólida antes da implementação dos módulos funcionais específicos de cada vertical.

---

## 2. Objetivo do TAGOX Flow

O objetivo do TAGOX Flow é fornecer uma plataforma centralizada para gestão de serviços profissionais, permitindo que diferentes organizações utilizem o mesmo sistema com segurança e isolamento de dados.

Cada organização cadastrada na plataforma representa um Tenant.

A arquitetura deve garantir:

- cada Tenant possui seus próprios usuários e dados;
- usuários possuem vínculo obrigatório com um Tenant;
- dados entre organizações permanecem isolados;
- acessos são controlados por regras de autorização;
- a plataforma pode evoluir para diferentes segmentos sem comprometer sua base.

O TAGOX Flow é concebido como um produto SaaS da TAGOX Tech.

---

## 3. Modelo SaaS

O TAGOX Flow utiliza o conceito de Software as a Service (SaaS).

Uma única aplicação atende múltiplas organizações clientes.

Cada organização possui uma representação lógica própria dentro da plataforma, denominada Tenant.

O modelo atual adotado é:

**Banco compartilhado + schema compartilhado + isolamento lógico por tenant_id.**

Nesse modelo, diferentes organizações utilizam a mesma infraestrutura, porém os dados permanecem separados através das relações de Tenant existentes no domínio.

A regra fundamental da arquitetura é:

> Nenhum Tenant pode acessar dados pertencentes a outro Tenant.

---


## 4. Arquitetura Multi-Tenant

### 4.1 Estratégia adotada

O TAGOX Flow utiliza o modelo:

Shared Database + Shared Schema + tenant_id

A aplicação utiliza uma única estrutura de banco de dados para múltiplas organizações.

O isolamento dos dados ocorre através do relacionamento entre as entidades e o Tenant responsável pelos registros.

A estrutura fundamental atual é:

Tenant
 |
 └── User
       |
       └── UserRole
              |
              └── Role

---

### 4.2 Conceito de Tenant

Tenant representa uma organização cliente dentro da plataforma.

Exemplos de organizações que poderão utilizar o TAGOX Flow:

- clínicas;
- consultórios;
- escritórios;
- empresas de serviços profissionais.

Cada Tenant possui seus próprios usuários e dados operacionais.

---

### 4.3 Isolamento de Dados

O isolamento entre organizações é um requisito fundamental da arquitetura.

Na implementação atual, o usuário possui uma referência obrigatória para seu Tenant através do campo:

tenant_id

Um usuário pertencente a um Tenant não deve acessar informações pertencentes a outro Tenant.

Essa regra também possui cobertura através de testes automatizados.

---

### 4.4 Integridade Multi-Tenant

A integridade da associação entre usuários e Tenants é protegida pelo banco de dados.

Regras atuais:

- usuário obrigatoriamente pertence a um Tenant;
- relacionamento protegido por chave estrangeira;
- não existe usuário sem organização vinculada;
- duplicidade de usuário é controlada por Tenant e email.

A restrição atual permite:

(tenant_id, email)

Isso significa:

- o mesmo email pode existir em organizações diferentes;
- o mesmo email não pode ser duplicado dentro do mesmo Tenant.

---

## 5. Stack Tecnológica

O TAGOX Flow está sendo desenvolvido utilizando uma stack moderna baseada em Java e Spring Boot.

### Backend

Tecnologias principais:

- Java 21;
- Spring Boot 4.1.1;
- Spring Data JPA;
- Hibernate;
- Spring Web MVC;
- Bean Validation;
- Maven.

### Banco de Dados

O banco de dados utilizado atualmente é:

- PostgreSQL.

O PostgreSQL foi escolhido por sua robustez, confiabilidade e capacidade de atender aplicações SaaS escaláveis.

### Migração de Banco

A evolução da estrutura do banco é controlada pelo:

- Flyway.

Todas as alterações estruturais devem ser realizadas através de migrations versionadas.

### Testes

O projeto possui testes automatizados utilizando:

- Spring Boot Test;
- JUnit;
- testes de persistência;
- testes de controllers;
- testes de regras de negócio.

A estratégia de testes tem como objetivo garantir estabilidade da base arquitetural antes da criação dos módulos funcionais.

---

## 6. Estrutura do Backend

O backend do TAGOX Flow segue uma organização baseada em domínio, separando responsabilidades entre entidades, serviços, controladores, DTOs e tratamento de exceções.

Estrutura principal:

controller

Responsável pelos endpoints HTTP da aplicação.

domain

Contém as entidades de negócio, enums e repositórios relacionados ao domínio.

dto

Contém objetos utilizados na comunicação entre API e aplicação.

service

Contém regras de negócio e operações de aplicação.

exception

Centraliza exceções de negócio e tratamento global de erros.

---

## 7. Domínio Tenant

Tenant representa a organização cliente dentro da plataforma.

Responsabilidades:

- identificar a organização;
- manter informações próprias da empresa;
- servir como base de isolamento dos dados.

O Tenant é a entidade principal da arquitetura multi-tenant.

Todos os usuários precisam estar vinculados a um Tenant.

A implementação atual possui:

- Tenant;
- TenantRepository;
- TenantService;
- TenantStatus;
- TipoPessoa.

---

## 8. Domínio User

User representa um usuário pertencente a uma organização dentro do TAGOX Flow.

Cada usuário possui:

- identificação própria;
- Tenant associado;
- nome;
- email;
- senha armazenada através de hash;
- status;
- informações de criação e atualização.

A associação obrigatória com Tenant garante que usuários estejam sempre vinculados a uma organização.

A implementação atual possui:

- User;
- UserRepository;
- UserStatus.

---

## 9. RBAC — Controle de Acesso

RBAC (Role-Based Access Control) é o modelo utilizado para controle de acesso baseado em papéis.

O objetivo é permitir que usuários possuam diferentes níveis de acesso conforme sua função dentro da organização.

O modelo inicial implementado é:

User → UserRole → Role

Os papéis atualmente definidos são:

- ADMIN;
- PROFISSIONAL;
- ASSISTENTE.

Nesta etapa do projeto, o RBAC representa a fundação para futuras permissões e regras de acesso mais específicas.

---


## 10. Domínio Role

Role representa um papel de acesso dentro do TAGOX Flow.

O objetivo da entidade Role é definir funções que podem ser atribuídas aos usuários.

A implementação atual possui:

- Role;
- RoleRepository;
- RoleType.

Os tipos de papel atualmente definidos são:

- ADMIN;
- PROFISSIONAL;
- ASSISTENTE.

A entidade Role possui:

- identificador UUID;
- tipo do papel;
- descrição.

O campo tipo utiliza enumeração persistida como texto, garantindo maior legibilidade no banco de dados.

---

## 11. Domínio UserRole

UserRole representa a associação entre um usuário e um papel de acesso.

Essa entidade funciona como tabela associativa entre User e Role.

O relacionamento atual é:

User → UserRole → Role

A implementação utiliza chave composta através de:

- UserRole;
- UserRoleId;
- UserRoleRepository.

A associação permite que um usuário possua papéis dentro da organização.

---

## 12. Relacionamentos do Modelo

O modelo atual do TAGOX Flow possui os seguintes relacionamentos principais:

Tenant possui vários usuários.

User pertence obrigatoriamente a um Tenant.

User pode possuir associações com papéis através de UserRole.

Role pode estar associado a múltiplos usuários através de UserRole.

Modelo conceitual:

Tenant
 |
 └── User
       |
       └── UserRole
              |
              └── Role

---

## 13. Banco de Dados e Flyway

O TAGOX Flow utiliza PostgreSQL como banco de dados principal.

A evolução estrutural do banco é controlada pelo Flyway.

O Hibernate está configurado para validar o schema existente utilizando:

spring.jpa.hibernate.ddl-auto=validate

Essa configuração garante que:

- o Hibernate não cria tabelas automaticamente;
- alterações estruturais devem ser feitas através de migrations;
- o modelo Java deve estar alinhado com o banco.

---

## 14. Histórico das Migrations

O estado atual do banco possui cinco migrations.

### V1 — create_tenant

Responsável pela criação inicial da estrutura de Tenant.

---

### V2 — alter_tenant_timestamps

Responsável pelos ajustes relacionados aos timestamps do Tenant.

---

### V3 — create_usuario

Responsável pela criação da tabela de usuários.

Inclui:

- vínculo com Tenant;
- dados cadastrais;
- controle de status;
- timestamps.

---

### V4 — create_role

Responsável pela criação da estrutura de papéis de acesso.

Inclui:

- identificação do papel;
- tipo;
- descrição.

---

### V5 — create_usuario_role

Responsável pela criação da associação entre usuários e papéis.

Utiliza chave composta:

(usuario_id, role_id)

---

## 15. Integridade e Regras de Segurança

As regras de integridade atuais são protegidas em diferentes camadas:

### Banco de Dados

- chaves primárias UUID;
- chaves estrangeiras;
- restrições de unicidade;
- campos obrigatórios.

### Aplicação

- validações de entrada;
- regras de negócio nos services;
- tratamento centralizado de exceções.

### Multi-Tenant

A arquitetura exige que toda informação pertencente a uma organização mantenha sua associação correta com o Tenant.

O isolamento entre organizações é uma regra fundamental do sistema.

---


## 16. DTOs

O TAGOX Flow utiliza DTOs (Data Transfer Objects) para controlar a comunicação entre a API e as camadas internas da aplicação.

Os DTOs possuem como objetivo:

- separar os modelos de entrada e saída das entidades do banco;
- evitar exposição direta das entidades JPA;
- controlar dados recebidos pela aplicação;
- facilitar validações e evolução dos contratos da API.

---

### 16.1 DTOs de Tenant

O domínio Tenant possui os seguintes DTOs:

- TenantRequestDTO;
- TenantResponseDTO.

TenantRequestDTO representa os dados recebidos para criação ou atualização de uma organização.

TenantResponseDTO representa os dados retornados pela API após o processamento.

---

### 16.2 DTOs de User

O domínio User possui os seguintes DTOs:

- CreateUserRequest;
- UserResponse.

CreateUserRequest representa os dados necessários para criação de um usuário.

UserResponse representa os dados retornados pela API.

---

A utilização de DTOs mantém uma arquitetura mais segura e permite que mudanças internas no modelo de domínio não afetem diretamente os consumidores da API.

---


## 17. Services

A camada de Services concentra as regras de negócio do TAGOX Flow.

Os Services são responsáveis por:

- executar operações de domínio;
- validar regras da aplicação;
- controlar fluxos de negócio;
- evitar que regras importantes fiquem dentro dos Controllers.

A separação entre Controller e Service permite uma arquitetura mais organizada, testável e preparada para evolução.

---

## 17.1 TenantService

O TenantService é responsável pelas operações relacionadas ao domínio Tenant.

Responsabilidades:

- criação de organizações;
- validação das regras de negócio do Tenant;
- consulta de informações da organização;
- integração entre Controller e Repository.

O TenantService atua como camada intermediária entre a API e a persistência dos dados.

---

## 17.2 UserService

O UserService controla as operações relacionadas aos usuários.

Responsabilidades:

- criação de usuários;
- validação dos dados recebidos;
- associação obrigatória com Tenant;
- regras relacionadas ao ciclo de vida do usuário.

Todo usuário criado no sistema deve possuir vínculo com uma organização.

---

## 17.3 UserRoleService

O UserRoleService é responsável pela associação entre usuários e papéis de acesso.

Responsabilidades:

- vincular usuários aos Roles disponíveis;
- controlar a relação User ↔ Role;
- preparar a base para futuras regras de permissão.

O modelo atual utiliza:

User → UserRole → Role

Essa camada será expandida futuramente com permissões mais detalhadas.

---


## 18. Controllers

Os Controllers representam a camada de entrada HTTP do TAGOX Flow.

São responsáveis por:

- receber requisições da API;
- validar dados recebidos através dos DTOs;
- encaminhar operações para os Services;
- retornar respostas HTTP adequadas.

Os Controllers não devem conter regras complexas de negócio.

A responsabilidade principal é realizar a comunicação entre o mundo externo e a camada de aplicação.

---

## 18.1 TenantController

O TenantController é responsável pelos endpoints relacionados às organizações.

Responsabilidades:

- receber solicitações relacionadas ao Tenant;
- utilizar TenantService para executar regras de negócio;
- retornar informações das organizações.

O Controller mantém a separação entre a camada HTTP e o domínio da aplicação.

---

## 18.2 UserController

O UserController é responsável pelos endpoints relacionados aos usuários.

Responsabilidades:

- receber solicitações de criação e consulta de usuários;
- utilizar UserService para execução das regras;
- trabalhar com DTOs de entrada e saída.

Todo usuário criado através da API deve respeitar as regras de associação com Tenant.

---

A arquitetura segue o fluxo:

Cliente HTTP
      |
      v
Controller
      |
      v
Service
      |
      v
Repository
      |
      v
Banco de Dados

---


## 19. Tratamento de Exceções

O TAGOX Flow possui uma camada dedicada para tratamento de erros da aplicação.

O objetivo é centralizar o gerenciamento de exceções, evitando tratamentos duplicados dentro dos Controllers.

Essa abordagem permite:

- respostas HTTP padronizadas;
- melhor experiência para consumidores da API;
- separação entre regras de negócio e tratamento de erros;
- maior facilidade de manutenção.

---

## 19.1 GlobalExceptionHandler

O GlobalExceptionHandler é responsável por capturar exceções lançadas pela aplicação e transformar esses erros em respostas HTTP adequadas.

Responsabilidades:

- interceptar exceções;
- definir códigos HTTP;
- padronizar mensagens de erro;
- retornar respostas consistentes para clientes da API.

---

## 19.2 BusinessException

BusinessException representa erros relacionados às regras de negócio da aplicação.

Exemplos:

- operações não permitidas;
- validações de domínio;
- violações de regras internas.

---

## 19.3 DuplicateResourceException

DuplicateResourceException representa tentativas de criação de recursos que já existem.

Exemplos:

- usuário duplicado dentro do mesmo Tenant;
- registros com restrições de unicidade violadas.

---

## 19.4 ResourceNotFoundException

ResourceNotFoundException representa situações onde um recurso solicitado não foi encontrado.

Exemplos:

- Tenant inexistente;
- usuário inexistente;
- recurso relacionado não localizado.

---

A centralização das exceções mantém a API mais previsível e facilita futuras evoluções do sistema.

---



## 20. Testes Automatizados

O TAGOX Flow possui uma estratégia de testes automatizados para validar a estabilidade da arquitetura antes da implementação dos módulos funcionais.

Os testes são utilizados para garantir:

- funcionamento correto das regras de negócio;
- integridade da persistência;
- comportamento dos Controllers;
- isolamento entre Tenants;
- funcionamento da base RBAC.

---

## 20.1 Testes de Tenant

Os testes relacionados ao Tenant validam:

- criação de organizações;
- regras de negócio;
- persistência dos dados;
- comportamento dos endpoints.

Testes existentes:

- TenantControllerTest;
- TenantPersistenceTest;
- TenantRepositoryTest;
- TenantServiceTest.

---

## 20.2 Testes de User

Os testes relacionados aos usuários validam:

- criação de usuários;
- validações de entrada;
- persistência;
- associação obrigatória com Tenant.

Testes existentes:

- UserControllerTest;
- UserControllerValidationTest;
- UserPersistenceTest.

---

## 20.3 Testes Multi-Tenant

O isolamento de dados entre organizações é validado através de testes específicos.

Teste existente:

- UserTenantIsolationTest.

Esse teste garante que usuários pertencentes a diferentes Tenants permaneçam isolados dentro da arquitetura.

---

## 20.4 Testes RBAC

O modelo inicial de controle de acesso possui validação através de:

- UserRoleServiceTest.

Esse teste valida a associação entre usuários e papéis:

User → UserRole → Role

---

A estratégia de testes permite evoluir o sistema com maior segurança, reduzindo riscos durante a criação dos próximos módulos.

---


## 21. Estado Atual do Projeto

O TAGOX Flow encontra-se atualmente na fase de construção da fundação arquitetural do produto.

O objetivo desta etapa é garantir uma base segura e escalável antes da implementação dos módulos específicos de negócio.

Estado atual:

- backend estruturado em Spring Boot;
- arquitetura organizada por domínio;
- banco PostgreSQL configurado;
- versionamento de banco utilizando Flyway;
- arquitetura multi-tenant implementada;
- entidades principais criadas;
- camada de persistência configurada;
- DTOs implementados;
- Services implementados;
- Controllers implementados;
- tratamento global de exceções implementado;
- testes automatizados criados.

---

### Tecnologias configuradas

Backend:

- Java 21;
- Spring Boot 4.1.1;
- Spring Data JPA;
- Spring Web MVC;
- Bean Validation;
- PostgreSQL;
- Flyway.

---

### Banco de Dados Atual

Migrations existentes:

- V1 — criação da tabela tenant;
- V2 — alteração de timestamps do tenant;
- V3 — criação da tabela usuario;
- V4 — criação da tabela role;
- V5 — criação da tabela usuario_role.

---

### Domínios Implementados

Atualmente existem os seguintes domínios:

Tenant

Responsável pela representação das organizações clientes.

User

Responsável pelos usuários vinculados aos Tenants.

Role

Responsável pelos papéis de acesso.

UserRole

Responsável pela associação entre usuários e papéis.

---


## 22. Decisões Arquiteturais

Esta seção registra as principais decisões técnicas tomadas durante a construção do TAGOX Flow.

O objetivo é manter histórico das escolhas arquiteturais e facilitar a evolução futura da plataforma.

---

## 22.1 Arquitetura Multi-Tenant

Foi escolhido o modelo:

Shared Database + Shared Schema + tenant_id.

Motivos:

- menor complexidade operacional;
- facilidade de manutenção;
- escalabilidade inicial;
- compartilhamento da mesma infraestrutura;
- isolamento lógico através das regras de domínio.

A arquitetura foi preparada para crescimento futuro, permitindo evolução conforme a necessidade do produto.

---

## 22.2 Utilização de UUID

As entidades principais utilizam UUID como identificadores.

Motivos:

- maior segurança em APIs públicas;
- evita exposição de IDs sequenciais;
- facilita integrações futuras;
- permite geração distribuída de identificadores.

---

## 22.3 Uso do Flyway

O controle do banco de dados é realizado através do Flyway.

Motivos:

- versionamento das alterações do banco;
- histórico das evoluções estruturais;
- previsibilidade entre ambientes;
- segurança durante mudanças de schema.

Nenhuma alteração estrutural do banco deve ser realizada manualmente em ambientes controlados.

---

## 22.4 Separação por Camadas

A arquitetura separa responsabilidades entre:

- Controller;
- Service;
- Repository;
- Domain;
- DTO.

Essa separação permite:

- melhor organização;
- testes mais simples;
- evolução independente das camadas;
- redução de acoplamento.

---

## 22.5 Uso de DTOs

Os DTOs foram adotados para evitar exposição direta das entidades de domínio.

Benefícios:

- controle dos contratos da API;
- segurança dos dados;
- facilidade de evolução;
- separação entre persistência e comunicação externa.

---

## 22.6 RBAC como Base de Segurança

O controle inicial de acesso foi construído utilizando RBAC.

Modelo:

User → UserRole → Role

Essa decisão permite iniciar com uma estrutura simples e evoluir futuramente para permissões mais detalhadas.

---

## 22.7 Construção Incremental do Produto

O TAGOX Flow está sendo desenvolvido seguindo uma abordagem incremental.

A prioridade inicial é construir o Core da plataforma:

- organizações;
- usuários;
- segurança;
- permissões;
- infraestrutura.

Após a consolidação do Core, serão implementados módulos específicos das verticais de negócio.

---


## 23. Funcionalidades Ainda Não Implementadas

O TAGOX Flow encontra-se atualmente na fase de construção do Core arquitetural.

As funcionalidades abaixo fazem parte do roadmap futuro e ainda não foram implementadas.

---

## 23.1 Segurança e Autenticação

Ainda serão implementados:

- autenticação de usuários;
- Spring Security;
- autenticação baseada em JWT;
- controle de sessão;
- recuperação de senha;
- políticas avançadas de segurança.

---

## 23.2 Controle de Permissões

O modelo RBAC atual representa a fundação inicial.

Futuramente serão adicionados:

- permissões específicas por recurso;
- ações permitidas por módulo;
- controle granular de acesso;
- políticas por organização.

---

## 23.3 Módulos de Negócio

Os módulos funcionais ainda serão desenvolvidos.

Exemplos:

- gestão de pacientes/clientes;
- agenda;
- prontuário;
- documentos;
- financeiro;
- relatórios;
- configurações específicas por vertical.

---

## 23.4 Vertical Psicologia e Saúde

A primeira vertical prevista para evolução do TAGOX Flow será a área de psicologia e saúde.

Essa implementação utilizará o Core existente:

- Tenant;
- Usuários;
- Segurança;
- Permissões;
- Estrutura SaaS.

O objetivo é criar uma solução especializada sem comprometer a reutilização da arquitetura para outras verticais.

---


## 24. Próximas Etapas

Após a consolidação da fundação arquitetural, as próximas etapas do desenvolvimento do TAGOX Flow serão realizadas de forma incremental.

---

## 24.1 Revisão da Fundação Atual

Antes da expansão dos módulos, será realizada uma revisão técnica dos componentes existentes:

- validação das migrations;
- revisão das entidades JPA;
- validação das regras multi-tenant;
- revisão das constraints do banco;
- melhoria dos testes automatizados.

---

## 24.2 Implementação da Segurança

A próxima grande evolução técnica será a implementação da camada de segurança.

Planejado:

- Spring Security;
- autenticação JWT;
- controle de acesso autenticado;
- proteção dos endpoints;
- integração com RBAC.

---

## 24.3 Evolução do Controle de Permissões

O modelo atual User → UserRole → Role será expandido para suportar:

- permissões por recurso;
- permissões por módulo;
- regras específicas por organização;
- controle granular de funcionalidades.

---

## 24.4 Construção dos Módulos de Negócio

Após a consolidação do Core, serão iniciados os módulos específicos da primeira vertical:

TAGOX Flow Psicologia e Saúde.

A implementação utilizará a base existente da plataforma.

---


## 25. Roadmap

O roadmap do TAGOX Flow está organizado em fases evolutivas, permitindo crescimento sustentável da plataforma.

---

## Fase 1 — Core da Plataforma (Atual)

Objetivo:

Construção da fundação SaaS.

Status:

Em desenvolvimento.

Entregas:

- arquitetura multi-tenant;
- gerenciamento de organizações;
- usuários;
- RBAC inicial;
- persistência;
- testes automatizados;
- estrutura backend.

---

## Fase 2 — Segurança e Identidade

Objetivo:

Implementar controle seguro de acesso à plataforma.

Entregas:

- autenticação;
- JWT;
- Spring Security;
- autorização baseada em permissões;
- proteção completa dos endpoints.

---

## Fase 3 — TAGOX Flow Psicologia e Saúde

Objetivo:

Construir a primeira vertical de negócio utilizando o Core existente.

Possíveis módulos:

- pacientes/clientes;
- agenda;
- atendimento;
- prontuário;
- documentos;
- relatórios;
- gestão operacional.

---

## Fase 4 — Expansão para Novas Verticais

A arquitetura foi preparada para permitir expansão futura.

Possíveis verticais:

- medicina;
- jurídico;
- outros serviços profissionais.

A estratégia é reutilizar o Core da plataforma e criar módulos específicos por segmento.

---

## Fase 5 — Evolução SaaS

Possíveis evoluções futuras:

- planos e assinaturas;
- cobrança recorrente;
- marketplace de integrações;
- relatórios avançados;
- recursos de inteligência e automação.

---


## 26. Considerações Finais

O TAGOX Flow está sendo desenvolvido com foco em uma arquitetura sólida, segura e preparada para crescimento.

A construção inicial prioriza a criação de um Core confiável, estabelecendo as bases necessárias para uma plataforma SaaS multi-tenant.

Os principais pilares arquiteturais definidos são:

- isolamento de dados entre organizações;
- organização por domínio;
- separação clara de responsabilidades;
- controle de acesso estruturado;
- versionamento seguro do banco de dados;
- testes automatizados;
- evolução incremental da plataforma.

As decisões arquiteturais registradas neste documento devem servir como referência para futuras implementações.

Novos módulos e funcionalidades deverão respeitar os princípios estabelecidos:

- segurança;
- escalabilidade;
- reutilização;
- manutenção simplificada;
- evolução sustentável.

O TAGOX Flow representa a base tecnológica para construção de soluções especializadas em diferentes verticais profissionais, iniciando pela área de psicologia e saúde e permitindo expansão futura para novos segmentos.

Este documento deve permanecer atualizado conforme novas decisões técnicas forem tomadas durante a evolução do produto.

---

