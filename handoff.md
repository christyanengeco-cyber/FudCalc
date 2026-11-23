# Handoff

## Estado
Fase 2 concluída: pacote `.fudpkg` embutido + importador idempotente + Room v2. 26 testes verdes, APK 0.2.0 instalado e verificado no SM-A366E sem perda dos registros do usuário.

## Próximo passo (Fase 3)
1. Base TACO/TBCA completa e auditada no mesmo formato (o usuário fornece o arquivo ou aprova uma extração pública verificada).
2. Importação de pacote escolhido pelo usuário (arquivo externo) — hoje só o asset embutido.
3. Exportação/importação dos dados do diário.

## Pendências menores
- Confirmar CI verde do commit da Fase 2.
- Teste automatizado da migração 1→2 exigiria `exportSchema = true` + `room-testing`.
- Opcional: `actions/checkout@v5` e `actions/setup-java@v5` (aviso Node 20).

## Cuidado
Editar `app/src/main/assets/foods/taco.fudpkg` exige recalcular o `sha256` do corpo em UTF-8. Não use `Get-Content -Raw` no PowerShell 5.1 para regravar o arquivo (corrompe acentos).

## Repo
https://github.com/christyanengeco-cyber/FudCalc (MIT, público)
