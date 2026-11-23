# Changelog

## 2026-11-23
- Fase 2: pacote nutricional `.fudpkg` (fonte, versão, licença, checksum SHA-256), importador idempotente, Room v2 (`food_packages`, `foods.packageId`, `foods.available`) com migração 1→2. `SeedFoods` removido. App 0.2.0.
- Testes: 26 (10 domínio + 12 parser + 4 importador Robolectric, incluindo leitura do asset real).
- Verificado no aparelho: upgrade 0.1.0 → 0.2.0 sobre banco com 3 registros reais do usuário; dados e total de 607 kcal intactos; pacote exibido em "Sobre".
- Bug corrigido: asset regravado por script PowerShell com decodificação Latin-1 gerava mojibake na fonte/versão; agora coberto por teste.
- `f0f521d` Corrige total de dia vazio, formatação de gramas e subtotal de refeição vazia.
- `7b2556d` Fase 1: fundação Android, domínio nutricional, Room e diário mínimo.
- Roadmap das fases 3 a 8 no README (base auditada e exportação → diário → produtos/OCR → receitas → foto → relatórios).
- Criados `init.md`, `changelog.md`, `handoff.md`.
