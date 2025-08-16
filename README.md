# Projeto Cafeteria

Este é um sistema de gestão para uma cafeteria, desenvolvido em Java com interface gráfica Swing e banco de dados MySQL.

## Descrição

O "Café & Brisa" é um sistema de ponto de venda (PDV) e gestão projetado para otimizar as operações diárias de uma cafeteria. Ele oferece funcionalidades distintas para diferentes tipos de usuários (administradores e atendentes), permitindo um controle eficiente desde o atendimento ao cliente até a gestão financeira e de estoque.

## Funcionalidades

O sistema possui dois perfis de acesso com diferentes funcionalidades:

### Painel do Administrador:

  * **Visão Geral Financeira:** Dashboard com o total de entradas e saídas.
  * **Gestão de Cardápio:** Adicionar, editar e remover itens do cardápio.
  * **Controle de Estoque:** Gerenciar a entrada de novos produtos e visualizar o estoque atual.
  * **Gestão de Usuários:** Cadastrar e gerenciar os usuários do sistema.
  * **Relatórios Financeiros:** Gerar relatórios de vendas e fluxo de caixa.
  * **Lançamento de Saídas:** Registrar despesas e outras saídas financeiras.

### Painel do Atendente:

  * **Novo Pedido:** Lançar novos pedidos de clientes.
  * **Consultar Pedido:** Visualizar o status e os detalhes dos pedidos realizados.

## Tecnologias Utilizadas

  * **Linguagem:** Java
  * **Interface Gráfica:** Swing
  * **Banco de Dados:** MySQL
  * **Conexão com Banco de Dados:** JDBC

## Como Configurar e Rodar o Projeto

Siga os passos abaixo para configurar e executar o projeto em sua máquina local.

### Pré-requisitos

  * Java Development Kit (JDK) 8 ou superior
  * MySQL Server
  * Uma IDE Java de sua preferência (Eclipse, IntelliJ IDEA, etc.)

### Banco de Dados

1.  **Crie o banco de dados:** Execute o script `SQL/cafeteria.sql` no seu MySQL para criar o banco de dados `cafeteria` e todas as tabelas necessárias.

2.  **Configure a conexão:** O sistema se conecta a um banco de dados local por padrão. Verifique se as credenciais no arquivo `conexao/Conexao.java` correspondem à sua configuração do MySQL:

      * **URL:** `jdbc:mysql://localhost:3306/cafeteria`
      * **Usuário:** `root`
      * **Senha:** `""` (vazio por padrão)

### Executando a Aplicação

1.  **Importe o projeto** na sua IDE Java.
2.  **Adicione o driver JDBC** ao seu projeto. O arquivo `mysql-connector-j-9.3.0.jar` já está na pasta `lib/`.
3.  **Execute a classe `Login.java`**, que é a porta de entrada do sistema.

## Estrutura do Projeto

O projeto está organizado da seguinte forma:

```
projeto-cafeteria/
├── SQL/
│   └── cafeteria.sql       # Script de criação do banco de dados
├── classes/                # Classes de modelo (POJOs)
├── conexao/
│   └── Conexao.java        # Classe de conexão com o banco de dados
├── lib/
│   └── mysql-connector-j-9.3.0.jar # Driver JDBC do MySQL
├── src/
│   ├── database/
│   ├── mail/
│   ├── models/
│   └── multitools/
└── telas/                  # Classes da interface gráfica (telas do sistema)
```
