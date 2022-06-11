package sagittarius

import GemContents.*
import java.net.URI
val lesson1 = Status.Success(
  List(
    H1("Data, Values, and Operators"),
    Text(
      "This post is meant to follow the landing page project in The Odin Project." +
        " Please follow The Odin Project up to that point and return here if you need to."
    ),
    Link(
      URI(
        "https://www.theodinproject.com/paths/foundations/courses/foundations/lessons/landing-page"
      ),
      "The Odin Project - Landing Page"
    ),
    Text("Otherwise, let's dive right in to Scala!"),
    H2("Learning Outcomes"),
    Text(
      "Look through these now and then use them to test yourself after doing the assignment:"
    ),
    ULItem("How do you define a value?"),
    ULItem("What are the rules for naming values?"),
    ULItem("What are operators, operands, and operations?"),
    ULItem(
      "What is concatenation and what happens when you add numbers and strings together?"
    ),
    ULItem("What are operator precedence values?"),
    ULItem("What are assignment operators?"),
    H2("How to Run Scala Code"),
    Text(
      "For now, you'll be running Scala via the Scala CLI tool developed by VirtusLab."
    ),
    Link(URI("https://scala-cli.virtuslab.org/install"), "Scala CLI tool"),
    Text(
      "Later lessons in Foundations and the Scala path will show you how to run Scala in the browser" +
        " and on the JVM using `mill`. For now, you should always default to running your Scala code via Scala CLI" +
        " unless otherwise specified. You may run into unexpected errors if you deviate from this advice."
    ),
    Text(
      "Installing the VirtusLab Scala CLI tool is relatively easy. All you need to do is run the " +
        "following two commands in the CLI:"
    ),
    Preformatted(
      "bash",
      """|curl -sSLf https://virtuslab.github.io/scala-cli-packages/scala-setup.sh | sh
         |source ~/.profile""".stripMargin
    ),
    Text(
      "Once you've completed this step, you're now able to run Scala code in three different ways:"
    ),
    ULItem("The Scala console"),
    ULItem("Compiling and running a Scala file"),
    ULItem("Compiling to Javascript with ScalaJS"),
    H3("Scala console"),
    Text(
      "To enter the Scala console you can run `scala-cli console`. You can run \"Hello, World\" " +
        "by entering the following:"
    ),
    Preformatted("scala", """|scala> "Hello, World!"""".stripMargin),
    Text(
      "You should see the output of this command in the line below when you press enter."
    ),
    Text("In order to exit the Scala console, write `:q` and press enter."),
    Text(
      "The Scala console is the quickest and easiest way to run and test Scala code. You will " +
        "find yourself using it to test your code even when you're an experienced programmer!"
    )
  )
)
