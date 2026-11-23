# FudCalc

Diário alimentar Android offline-first, foco em alimentos brasileiros. Kotlin + Jetpack Compose + Room.

Estado: **Fase 1 (fundação + diário mínimo)**. Nada aqui é simulação: os cálculos, o banco e o diário são reais; o que não existe está listado como não implementado.

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

## Semente de alimentos (atenção)

`data/SeedFoods.kt` traz 11 itens da TACO 4ª ed. com valores por 100 g do estado de preparo. É um subconjunto **não auditado linha a linha**: **a Fase 2 deve substituí-lo pelo pacote oficial importado** e remover esta semente. Não use esta base para decisões clínicas.

Medidas caseiras (colher, concha, unidade) são aproximações; variam por utensílio. O app deixa claro que são aproximadas.

## Testes

`app/src/test/java/com/fudcalc/domain/nutrition/NutritionMathTest.kt` — escala por gramas, zero vs ausente, propagação de desconhecido, origem calculada, somas, arredondamento HALF_UP, medida caseira, quantidade inválida.

Cobertura de banco (importação/atualização/duplicação) exige testes instrumentados ou Robolectric: entra junto com o importador de pacotes (Fase 2).

### Verificação em aparelho (23/11/2026)

Samsung SM-A366E, Android 17, APK debug instalado via adb. Verificado: semente carregada, busca, 150 g de arroz branco = 192 kcal (128/100 g), refeição e totais atualizados, exclusão com confirmação, dia vazio = 0 kcal, nenhum crash no logcat. Não verificado: fontes grandes, tablet, rotação, restauração de backup.

## Licenças

- Código: MIT (ver `LICENSE`).
- TACO: dados do projeto TACO/NEPA-UNICAMP; redistribuição deve ser verificada antes de embarcar o pacote completo.
- AndroidX/Compose/Room/Kotlin: Apache 2.0.

## Próxima etapa (Fase 2)

Importador de pacotes nutricionais com versão, licença, checksum e idempotência; modelo de proveniência completo; busca com medidas; testes de banco. Só depois: diário completo (favoritos, reutilizar refeição) e produtos.
