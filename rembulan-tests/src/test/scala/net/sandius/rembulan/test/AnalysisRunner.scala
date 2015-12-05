package net.sandius.rembulan.test

import net.sandius.rembulan.core.{PrototypePrinter, LuaCPrototypeLoader}

object AnalysisRunner {

  def main(args: Array[String]): Unit = {

    val luacPath = "/Users/sandius/bin/luac53"
    require (luacPath != null)

    val ploader = new LuaCPrototypeLoader(luacPath)

//    val program =
//      """
//        |if x >= 0 and x <= 10 then print(x) end
//      """.stripMargin

    val program =
    """function f(x)
      |  print(x)
      |  if x > 0 then
      |    return f(x - 1)
      |  else
      |    if x < 0 then
      |      return f(x + 1)
      |    else
      |      return 0
      |    end
      |  end
      |end
      |
      |function g()
      |  for i = 'x', 0 do print(i) end
      |end
      |
      |return f(3),f(-2)
      """.stripMargin

//    val program =
//      """local f = function (x, y)
//        |    return x + y
//        |end
//        |return -1 + f(1, 3) + 39
//      """.stripMargin

//    val program =
//      """local f = function (x, y, z)
//        |    return x + y + z
//        |end
//        |return -1 + f(1, 1, 2) + 39
//      """.stripMargin

    println(program)

    val proto = ploader.load(program)

    PrototypePrinter.print(proto, System.out)

    println()
    println("Control flow")
    println("------------")
    println()

    println("Main (" + PrototypePrinter.pseudoAddr(proto) + "):")
    new ControlFlowTraversal(proto).print(System.out)

    val it = proto.getNestedPrototypes.iterator()
    while (it.hasNext) {
      val child = it.next()
      println()
      println("Child (" + PrototypePrinter.pseudoAddr(child) + "):")
      new ControlFlowTraversal(child).print(System.out)
    }

  }

}