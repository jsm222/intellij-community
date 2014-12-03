package mockLib.foo

public class LibClass {
    public fun foo() {
    }

    class object {
        fun classObjectFun() {
        }

        public object NestedObject
    }

    public class Nested {
        public val valInNested: Int = 1
        public fun funInNested() {
        }
    }

    public val nested: Nested = Nested()
}

public trait LibTrait {
    public fun foo() {
    }
}

public enum class LibEnum {
    RED
    GREEN
    BLUE
}

public object LibObject

public fun topLevelFunction(): String = ""

public fun String.topLevelExtFunction(): String = ""

public var topLevelVar: String = ""

class F() {
    class object {
        class F {
            class object {
                object F {
                }
            }
        }
    }
}