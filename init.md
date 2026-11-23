# FudCalc — init

App Android de diário nutricional, offline, Kotlin + Compose + Room.

## Regras do produto
- Offline por construção (sem permissão INTERNET).
- Nunca inventar valores nutricionais. Distinguir zero / desconhecido / não informado / estimado / calculado.
- Cálculo nutricional sempre em código determinístico, nunca em LLM.
- Preservar fonte, versão e estado de preparo do dado.
- Sem contas, anúncios ou telemetria. Dados exportáveis. Fotos locais.
- Fases pequenas e verificáveis; reportar só o que foi realmente testado.

## Stack
- minSdk 26 / target 35, AGP 8.10.1, Kotlin 2.1.21, KSP 2.1.21-2.0.1, Compose BOM 2025.05.00, Room 2.7.1.
- Módulo único `app`. Sem navigation-compose: 3 abas por estado (Diário, Alimentos, Sobre).

## Estrutura
- `app/src/main/java/com/fudcalc/domain/nutrition/` — matemática pura: `NutrientValue`, `NutritionFacts`, `factsForGrams`, `factsForPortion`, `sumFacts`, `roundTo`.
- `core/database/` — Room: `NutrientColumns` (nutrientes em TEXT), `FoodEntity`, `DiaryEntryEntity`, `NutrientValueConverter`.
- `data/` — `FoodRepository` (busca normalizada, info do pacote), `packages/` (formato `.fudpkg`, parser, importador).
- `app/src/main/assets/foods/taco.fudpkg` — pacote embutido (11 itens TACO, subconjunto não auditado).
- `ui/` — `DiaryScreen`, `FoodSearchScreen`, `AboutScreen`, `Format.kt`, tema.

## Decisões que não voltar atrás sem discutir
- Nutrientes persistidos como TEXT para manter Unknown ≠ NotInformed ≠ zero.
- Entrada do diário guarda snapshot por 100 g + `referenceGrams`: atualizar alimento não reescreve histórico.
- Soma com qualquer parcela desconhecida = desconhecido. Dia vazio = zero `Calculated`.
- Pacote nutricional em texto (`.fudpkg`) com checksum SHA-256 do corpo; import idempotente por checksum; item removido fica `available = 0`, nunca apagado.
- Ao editar o asset, recalcular o `sha256` **em UTF-8** (PowerShell `Get-Content -Raw` corrompe acentos; usar `[IO.File]::ReadAllBytes` + `UTF8.GetString`).

## Comandos
```powershell
$env:JAVA_HOME = (Get-ChildItem "C:\Program Files\Eclipse Adoptium" -Directory | Where-Object Name -like "jdk-17*").FullName
.\gradlew.bat testDebugUnitTest assembleDebug
```
APK: `app\build\outputs\apk\debug\app-debug.apk`
adb: `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe` (aparelho SM-A366E, tela 1080x2340)

## Limites conhecidos
- Pacote embutido é subconjunto de 11 itens TACO, não auditado item a item. Medidas caseiras aproximadas.
- Sem importação de arquivo externo, export/import do diário, favoritos, receitas, scanner/OCR.
- Room v2; migração 1→2 sem teste automatizado (verificada no aparelho).
