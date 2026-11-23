package com.fudcalc.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private const val TEXT = """
FudCalc 0.1.0 — diário alimentar local.

FUNCIONA OFFLINE
Todos os dados ficam no banco local do aparelho. Nenhum registro é enviado para servidores. O aplicativo não pede conta e não tem telemetria.

FONTES DOS DADOS
Os valores vêm identificados com fonte e versão. Esta versão traz uma semente pequena da TACO 4ª ed.; o pacote nutricional completo e verificado entra na Fase 2. Campos sem dado aparecem como "—" (fonte não possui) ou "n/i" (campo vazio na fonte). Nunca são exibidos como zero.

CÁLCULOS
Calorias e nutrientes são proporcionais à quantidade informada em gramas, a partir da base de 100 g do alimento. Medidas caseiras são convertidas para gramas aproximados antes do cálculo. Todos os valores do diário são calculados, não medidos.

LIMITAÇÕES DESTA VERSÃO
- Sem importação de pacotes TACO/TBCA.
- Sem scanner de código de barras ou Open Food Facts.
- Sem OCR de rótulos.
- Sem receitas.
- Sem análise por fotografia.
- Sem exportação/importação de dados.
- Refeições personalizadas e favoritos ainda não existem.

PRIVACIDADE E BACKUP
O backup do Android (quando ativado no sistema) pode copiar o banco do aplicativo para a nuvem do usuário. O FudCalc não controla esse comportamento. Para remover tudo, limpe os dados do app nas configurações do Android.

LICENÇA
Código sob licença MIT. As bases de dados e modelos mantêm suas próprias licenças; a licença do aplicativo não as substitui.
"""

@Composable
fun AboutScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        Text(
            TEXT.trimIndent(),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
