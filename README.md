# Lista de Compras

App Android de lista de compras inspirado no Listonic. Kotlin + Jetpack Compose (Material 3), dados salvos no aparelho com Room.

## O que a versão 1.0 faz

- **Várias listas** com progresso ("3 de 10 itens") e total em R$ na tela inicial; renomear, duplicar e excluir.
- **Adicionar rápido**: digite e toque Enter. Aceita quantidade junto: `2 leite`, `carne 1,5 kg`, `ovos 1 dz`. Se o item já está na lista, soma em vez de duplicar.
- **Sugestões ao digitar**: catálogo com ~300 produtos comuns em mercados brasileiros + tudo o que você já usou (lembra seção, unidade e último preço).
- **Seções do mercado**: itens agrupados automaticamente (Hortifrúti, Padaria, Carnes, Laticínios…), na ordem de um percurso típico.
- **Preços e total**: preço por un/kg/L; em g e ml o preço é por kg/L, como na etiqueta. Barra inferior com total da lista e total já no carrinho, e aviso de itens sem preço.
- **Marcar como comprado**: o item vai para "No carrinho" (recolhível). Desmarcar todos / remover comprados.
- **Editar item**: nome, quantidade (−/+), unidade, preço, seção e observação. Excluir com "Desfazer".
- **Compartilhar** a lista como texto (WhatsApp, e-mail…).
- Tema claro/escuro automático.

## Como o APK é gerado

Não é preciso abrir o Android Studio. O arquivo `.github/workflows/build-apk.yml` compila o app no GitHub a cada envio para a branch `main` e publica o APK em **Releases**. No celular, abra a página de Releases do repositório, baixe `ListaDeCompras.apk` e instale (o Android pede para permitir "instalar apps desconhecidos" na primeira vez).

Cada versão nova instala por cima da anterior sem perder as listas, porque todas são assinadas com a mesma chave (`app/keystore/app.jks`). **Mantenha o repositório privado** — a senha da chave está no `app/build.gradle.kts`.

## Estrutura

```
app/src/main/java/com/listadecompras/app/
├── data/          Room (entidades, DAO, banco), repositório, catálogo e seções
├── ui/lists/      Tela "Minhas listas"
├── ui/detail/     Tela da lista, sugestões, editor de item, barra de totais
├── ui/components/ Diálogos
├── ui/theme/      Cores e tipografia
└── util/          Formatação em R$, parser de "2 leite"
```

## Ideias para próximas versões

Compartilhamento em tempo real entre pessoas (Firebase), histórico de preços por mercado, ditado por voz, widget na tela inicial, ordenação personalizada das seções.
