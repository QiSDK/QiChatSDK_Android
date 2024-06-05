
import com.teneasy.sdk.Line

data class AppConfig(
  val code: Long,
  val version: String,
  val name: String,
  val token: String,
  val publicKey: String,
  val lines: List<Line>,
)


