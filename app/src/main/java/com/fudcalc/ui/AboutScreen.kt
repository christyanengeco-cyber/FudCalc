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
import com.fudcalc.data.FoodRepository

private const val TEXT = """
FudCalc 0.2.0 — diário alimentar local.

FUNCIONA OFFLINE
Todos os dados ficam no banco local do aparelho. Nenhum registro é enviado para servidores. O aplicativo não pede conta e não tem telemetria.

PACOTE NUTRICIONAL
{pacote}

FONTES DOS DADOS
Os valores vêm identificados com fonte e versão, e o pacote importado é verificado por checksum (SHA-256). Campos sem dado aparecem como "—" (fonte não possui) ou "n/i" (campo vazio na fonte). Nunca são exibidos como zero. Atualizar o pacote não altera registros já lançados: cada registro guarda uma cópia dos valores usados no momento.

CÁLCULOS
Calorias e nutrientes são proporcionais à quantidade informada em gramas, a partir da base de 100 g do alimento. Medidas caseiras são convertidas para gramas aproximados antes do cálculo. Todos os valores do diário são calculados, não medidos.

LIMITAÇÕES DESTA VERSÃO
- O pacote embutido é um subconjunto de 11 alimentos da TACO, não a tabela completa; os valores não foram auditados item a item contra a fonte.
- Sem importação de arquivo externo escolhido pelo usuário (só o pacote embutido).
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
fun AboutScreen(packageInfo: FoodRepository.PackageInfo?, importError: String? = null) {
    val packageLine = when {
        importError != null -> "Falha ao importar: $importError"
        packageInfo == null -> "Importando…"
        else -> "${packageInfo.source} — ${packageInfo.version}\n${packageInfo.itemCount} alimentos disponíveis."
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        Text(
            TEXT.trimIndent().replace("{pacote}", packageLine),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
