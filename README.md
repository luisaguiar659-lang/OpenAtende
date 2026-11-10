# OpenAtende

Aplicativo Android de respostas automáticas para notificações do WhatsApp Business.

## Objetivo

O OpenAtende monitora notificações autorizadas pelo usuário, identifica mensagens recebidas e, quando a notificação disponibilizar uma ação de resposta compatível, envia uma resposta automática conforme regras locais configuráveis.

## Escopo inicial

- Aplicativo Android nativo em Kotlin.
- Tela inicial com status da automação.
- Orientação para conceder acesso às notificações.
- Serviço `NotificationListenerService` para observar notificações do WhatsApp Business.
- Camada separada para identificar mensagens e selecionar respostas.
- Preparação para gatilhos configuráveis individualmente: correspondência exata ou palavra/frase contida.
- Resposta pela ação `RemoteInput` da notificação, somente quando disponível.
- Controles para ativar/desativar a automação e evitar respostas duplicadas.

## Limitações importantes

O Android e o WhatsApp Business controlam quais notificações e ações de resposta ficam disponíveis. O aplicativo não deve simular toques, ler conversas fora das notificações, nem prometer resposta quando não houver uma ação compatível. O funcionamento depende das permissões, configurações de notificação, versão do Android e comportamento do WhatsApp Business. Esta solução não é uma API oficial do WhatsApp.

## Privacidade

As mensagens devem ser processadas localmente no aparelho. Não enviar conteúdo das notificações para servidores. Solicitar apenas as permissões necessárias e explicar claramente o acesso especial às notificações.

## Próximas etapas

1. Criar o projeto Android/Kotlin e a interface inicial.
2. Implementar a tela de autorização de acesso às notificações.
3. Implementar e testar a captura de notificações em dispositivos reais.
4. Detectar ações de resposta rápida compatíveis e testar o envio.
5. Construir a tela de cadastro de regras e respostas (a configuração será feita posteriormente).
6. Adicionar histórico local, proteção contra duplicidade e controles de execução.

## Estado atual

Foi criado o esqueleto inicial do projeto Android:

- Configuração Gradle Kotlin DSL e módulo `app`.
- Tela inicial com status do acesso às notificações e controle liga/desliga.
- Declaração do `NotificationListenerService`.
- Verificação do acesso especial às notificações.
- Modelo e mecanismo inicial de regras com modos `CONTAINS` e `EXACT`; a lista de regras permanece vazia.
- Tentativa de resposta somente por ação `RemoteInput` disponível na própria notificação do WhatsApp Business.

Ainda não foi gerado nem testado um APK em dispositivo real. A próxima etapa é compilar, corrigir eventuais incompatibilidades e testar a leitura e resposta às notificações em diferentes versões do Android e do WhatsApp Business.
