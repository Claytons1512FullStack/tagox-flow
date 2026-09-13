# TAGOX Flow - Arquitetura Multi-Tenant

## 1. Objetivo

O TAGOX Flow é uma plataforma SaaS multi-tenant desenvolvida pela TAGOX Tech.

O sistema deve permitir que múltiplas empresas utilizem a mesma aplicação mantendo:

- isolamento de dados;
- segurança entre organizações;
- escalabilidade;
- rastreabilidade;
- controle de permissões.

Cada empresa cadastrada no sistema representa um Tenant.

---

# 2. Conceito de Tenant

Tenant representa uma organização cliente dentro da plataforma.

Exemplos:

- Clínica de Psicologia;
- Consultório Médico;
- Escritório Jurídico;
- Empresas de serviços profissionais.

Cada Tenant possui seus próprios:

- usuários;
- configurações;
- clientes/pacientes;
- módulos contratados;
- dados operacionais.

Nenhum dado de um Tenant pode ser acessado por outro.

---

# 3. Estratégia Multi-Tenant

## Modelo escolhido

O TAGOX Flow utilizará:

**Banco compartilhado + schema compartilhado + isolamento por tenant_id**

Modelo:
