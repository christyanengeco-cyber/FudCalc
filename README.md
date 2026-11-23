# FudCalc

Diário alimentar Android offline-first, foco em alimentos brasileiros. Kotlin + Jetpack Compose + Room.

Estado: **Fase 2 (pacote nutricional versionado + diário mínimo)**. Nada aqui é simulação: os cálculos, o banco e o diário são reais; o que não existe está listado como não implementado.

## Como compilar

Requisitos: JDK 17 e Android SDK 35 (`platforms;android-35`, `build-tools;35.0.0`).

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17..."
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`
Instalar: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

## Decisões da Fase 1

- **Um módulo Gradle** (`app`) com pacotes internos por responsabilidade. Modularizar só quando houver necessidade concreta.
- **Nutrição é domínio puro** (`domain/nutrition`), sem Android. Fórmulas em `NutritionFacts.kt`.
- **Ausência de dado é explícita**: `NutrientValue.Value` (medido/estimado/calculado), `Unknown` (fonte não possui) e `NotInformed` (campo vazio). Zero é `Value(0.0)`. Valores são gravados como texto no Room para preservar essa distinção.
- **Snapshot no diário**: cada registro guarda os valores por 100 g do momento da inserção. Atualizar a base não reescreve o histórico.
- **Somas honestas**: se qualquer parcela de um nutriente é desconhecida, o total exibe "—" em vez de um total parcial disfarçado.
- **Escala**: valor medido escalado vira `Calculated` (ex.: 150 g), preservando a origem.
- **Interface sem regra nutricional**: telas só chamam ViewModel/repositórios.
- Sem `navigation-compose` ainda: 3 abas com estado simples. Introduzir navegação quando as telas de captura/OCR entrarem.

## Pacote nutricional (atenção)

`app/src/main/assets/foods/taco.fudpkg` é o pacote embutido: cabeçalho com id, nome, versão, fonte, licença e `sha256` do corpo; depois uma linha por alimento (13 campos separados por `;`). Campo vazio = desconhecido, `ni` = não informado, `0` = zero real. Porções em `rótulo=gramas`.

Importação (`data/packages/`): valida formato e checksum antes de tocar no banco; mesma checksum não reimporta; itens são atualizados por id estável; item que sai do pacote fica `available = 0` (não é apagado); porções só são regravadas quando o conteúdo muda. O diário nunca é reescrito.

O conteúdo atual são **11 itens da TACO 4ª ed., não auditados linha a linha** — o mecanismo está pronto, a base completa ainda não. Não use para decisões clínicas. Para trocar a base, edite o arquivo e recalcule o `sha256` do corpo (tudo após a linha `---`, em UTF-8, com `\n`).

Medidas caseiras (colher, concha, unidade) são aproximações; variam por utensílio. O app deixa claro que são aproximadas.

## Testes

- `domain/nutrition/NutritionMathTest.kt` — escala por gramas, zero vs ausente, propagação de desconhecido, origem calculada, somas, arredondamento HALF_UP, medida caseira, quantidade inválida.
- `data/packages/FoodPackageTest.kt` — cabeçalho, checksum divergente, CRLF, coluna faltando com número da linha, id repetido, valor negativo, energia obrigatória, porção inválida, pacote vazio, desconhecido vs zero vs não informado.
- `data/packages/FoodPackageImporterTest.kt` (Robolectric + Room em memória) — import idempotente, atualização de valores, item adicionado/removido, diário preservado, pacote inválido não altera o banco, e o asset real importado com acentos corretos.

26 testes. A migração 1→2 não tem teste automatizado (schema não exportado); foi verificada no aparelho.

### Verificação em aparelho (23/11/2026)

Samsung SM-A366E, Android 17, APK debug via adb.

Fase 1: semente carregada, busca, 150 g de arroz = 192 kcal, exclusão com confirmação, dia vazio = 0 kcal.

Fase 2: atualização 0.1.0 → 0.2.0 sobre banco v1 **com 3 registros reais do usuário** — migração executada, os 3 registros e o total de 607 kcal intactos; pacote importado (11 alimentos) e exibido em "Sobre" com fonte e versão; busca sem acento ("feijao") encontra "Feijão"; quantidade inválida mantém "Adicionar" desabilitado; nenhum crash no logcat.

Bug achado e corrigido no teste: o asset havia sido regravado por um script PowerShell que decodificou UTF-8 como Latin-1, gerando mojibake ("4Âª", "â€“") na fonte/versão. Corrigido e coberto por teste que lê o asset real.

Não verificado: base grande (dezenas de milhares de itens), tablet, rotação, restauração de backup, migração a partir de banco com alimentos criados pelo usuário.

## Licenças

- Código: MIT (ver `LICENSE`).
- TACO: dados do projeto TACO/NEPA-UNICAMP; redistribuição deve ser verificada antes de embarcar o pacote completo.
- AndroidX/Compose/Room/Kotlin: Apache 2.0.

## Próxima etapa (Fase 3)

Base TACO/TBCA completa e auditada dentro do mesmo formato de pacote; importação de arquivo escolhido pelo usuário; exportação/importação dos dados do diário. Só depois: favoritos, reutilizar refeição, produtos e scanner.
