# RFTroca

RFTroca é um plugin Paper para Minecraft que adiciona um sistema de trocas seguras entre jogadores usando uma interface gráfica compartilhada.

Desenvolvido por [sxnifero](https://github.com/sxnifero).

## Funcionalidades

- Pedido de troca com `/trocar <nick>`.
- Botões clicáveis no chat para aceitar ou negar a troca.
- Interface com áreas separadas para cada jogador.
- Confirmação individual dos dois jogadores.
- Contagem regressiva configurável antes da conclusão.
- Expiração automática de pedidos pendentes.
- Cancelamento automático se alguém fechar o menu, sair do servidor ou alterar a oferta.
- Devolução dos itens ao cancelar a troca.
- Drop automático no chão caso o inventário do jogador esteja cheio.
- Mensagens e regras básicas configuráveis em `config.yml`.

## Requisitos

- Java 21.
- Servidor Paper compatível com API `1.21`.
- Maven 3.9+ para compilar.

## Dependências externas

O plugin não usa banco de dados, Redis, APIs web ou outros plugins obrigatórios.

A única dependência do projeto é a `paper-api`, usada apenas para compilação com escopo `provided`. O servidor Paper fornece essa API em tempo de execução.

## Como compilar

```bash
mvn clean package
```

O arquivo compilado será gerado em:

```text
target/RFTroca-1.0.jar
```

## Como instalar

1. Compile o projeto com Maven.
2. Copie `target/RFTroca-1.0.jar` para a pasta `plugins/` do servidor.
3. Reinicie o servidor.

## Comandos

| Comando | Descrição |
| --- | --- |
| `/trocar <nick>` | Envia um pedido de troca para outro jogador online. |
| `/trade <nick>` | Alias de `/trocar`. |
| `/troca <nick>` | Alias de `/trocar`. |

Os subcomandos `/trocar aceitar <nick>` e `/trocar negar <nick>` são usados internamente pelos botões do chat.

## Permissões

| Permissão | Padrão | Descrição |
| --- | --- | --- |
| `rftroca.usar` | Todos | Permite usar o sistema de trocas. |

## Configuração

O arquivo `config.yml` é criado automaticamente na primeira inicialização do plugin.

```yaml
trade:
  countdown-seconds: 5
  minimum-items: 1
  request-expiration-seconds: 60
```

- `countdown-seconds`: tempo da contagem regressiva depois que os dois jogadores aceitam.
- `minimum-items`: quantidade mínima de itens que o jogador precisa colocar para aceitar.
- `request-expiration-seconds`: tempo até um pedido pendente expirar.

As mensagens também ficam em `config.yml` e usam `&` para cores.

## Estrutura do projeto

```text
src/main/java/redefenix/troca/
  RFTrocaPlugin.java   Classe principal do plugin
  TrocaCommand.java    Comando /trocar e pedidos pelo chat
  TrocaListener.java   Eventos da interface de troca
  TrocaManager.java    Controle de pedidos e sessões ativas
  TrocaSession.java    Lógica da interface e conclusão da troca

src/main/resources/
  config.yml           Configurações e mensagens padrão
  plugin.yml           Metadados do plugin para o Paper
```

## Limitações conhecidas

- As trocas acontecem apenas entre jogadores online.
- O plugin não salva histórico de trocas.
- Não há integração com economia, logs externos ou banco de dados.
- As mensagens são configuráveis, mas ainda não há comandos administrativos para recarregar a configuração em tempo real.

## Licença

Este projeto usa a licença MIT. Consulte o arquivo `LICENSE` para mais detalhes.
