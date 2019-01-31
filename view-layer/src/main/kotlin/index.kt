import react.*
import react.dom.*
import kotlin.browser.*

class App : RComponent<RProps, RState>() {
	override fun RBuilder.render() {
		div("App-header") {
			h2 {
				+"Welcome to React with 	Kotlin"
			}
		}
		p("App-intro") {
			+"To get started, edit "
			code { +"app/App.kt" }
			+" and save to reload."
		}
		p("App-ticker") {
		}
	}
}

fun RBuilder.app() = child(App::class) {}

fun main(args: Array<String>) {
  render(document.getElementById("root")) {
    app()
  }
}
