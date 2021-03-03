## Candy Language
一个简单的动态类型编程语言（支持面向对象，函数式编程，闭包等特性）。
自己编写的玩具脚本语言，还有很多不足和改进的地方。

## 构建
Candy 通过 Java 语言编写，需要 JRE(Java Runtime Environment) 去运行 Candy。
Candy 所需要的 Java8 及其以上的版本才能编译、运行。

下载项目并且编译：
``` shell
git clone git@github.com:nano-go/JCandy-Lang.git
cd JCandy-Lang

# 生成的 Jar 包在 test 目录下
gradle makeJar
```

这里并不一定需要安装 Gradle，因为没有什么依赖，用 javac 也是可以的。

## 运行
``` shell
cd test/

# 运行整个测试目录下所有的 Candy 源文件
java -jar candy.jar src/
```

如果没有参数，则进入交互式命令行。

### Disassemble

可以通过 dis 工具来查看 Candy 源文件生成的指令。
``` shell
java -jar candy.jar dis tmp.cd
```


## 语法
Candy 的一些语法有一点类 C 语言语法，其语句都是使用大括号所包裹的，也有 `if`, `for`, `while` 这样的流程控制语法。

在 Candy 中，一个语句的结尾并不需要显示的去加上一个 ';'，例如：
```
println(a + b)
a + b; // 也不会报错，这个 ';' 是可选的
```

### 注释:
```
// Single line comment.

/**
 * Hi,
 * Hello.
 */
```

### 变量声明和赋值：
```
var a = 0
// or
var a // equal to var a = null

a = 15
a = "15"
```

可以在一个作用域声明相同命名的变量，编译器并不会发出警告。
```
var a = 5
var a = "15"
println(a) // print 15.

{
  var a = 5
  var a = "15"
  println(a) // print 15.
}
```

### If 语句
Candy 中任意类型都可以作为 `Bool` 类型，其中除了 `null` 和 `false` 以外，其他的都为 `true`.

简单 If 语句：
```
var a = 5
if (a) {
  println(a)
}
// will print 5.
```

Else 块：
```
if (true)
  if (false)
    print(0)
  else
    print(1)
// will print 1.
```

### While 循环
```
while (1) {
	println("Read Read Read.")
}
```

支持 Continue 和 Break 语句（不写例子了）。

### For In Loop
Candy 中所有实现 `_iterator()` 方法的类都可以使用 `For In Loop`.

```
for (i in range(0, 15))
  println(i)
// will print 0..14

for (i in [0, 1, 2, 3])
  println(i)
// will print 0..4
```

### 函数

定义一个函数:
```
fun doubleN(n) {
  return n*n
}
```

如果最后是表达式语句，可以忽略 `return`，编译器会自动插入 `return` 语句。
```
fun doubleN(n) {
  n * n // equal to 'return n*n'
}

fun doubleN(n) {
  {
    n * n // equal to 'return n*n'
  }
}
```

但如果是分支或者循环内的最后一条语句，编译器并不会插入 `return` 语句。
```
fun doubleN(n) {
  if (n == 0)
    0
  else
    n*n
}
```
此时默认情况下返回 `null`.

### 闭包和 Lambda 表达式
Candy 支持闭包和 Lambda 表达式（匿名函数）。

Closure:
```
fun loop() {
  var iGetter
  for (i in range(0, 10)) {
    fun getI() {
      return i
    }
    iGetter = getI
  }
  return iGetter
}

// Currying Style
println(loop()())
// will print 9
```

Lambda:
```
fun foreach(arr, f) {
  for (e in arr) {
     f(e)
  }
}

var sum = 0
foreach([1, 2, 3], lambda e -> sum += e)
println(sum)
// will print 6

foreach([1, 2, 3], lambda e -> {
  sum += range(0, e).rand()
})
println(sum)
// will print some integer in range [6, 12]
```

### 类定义

Candy 是面向对象的编程语言，拥有类和继承的特性。

类定义:
```
class Ref {
  /**
   * 初始化器。
   */
  init(value) {
    this.value = value
  }
}

// 创建对象时会调用初始化器。
// Ref 的参数个数为 Ref 内初始化器的参数个数。
var ref = Ref(15)
println(ref.value)
// will print 15
```

方法定义：
```
class Ref {
  /**
   * 初始化器。
   */
  init(value) {
    this.value = value
  }
	
  /**
   * 你可以在 'getRef' 前添加 'fun' 关键字，这是可选的语法。
   */
  /*fun*/ getRef() {
    return this.value
  }
}

var ref = Ref(15)
println(ref.getRef())
// will print 15
```

Candy 中的方法也有类似于 Python 中的 `self` 指针（在 Candy 中它叫 `this`），但你不需要显示地声明它为一个参数。

在 Candy 中必须使用 `this` 关键字来表示类的实例。
```
class Ref {

  init(value) {
    this.value = value
  }
	
  getRef() {
    // return value // error
    return this.value
  }
}
```

### 继承
可以通过 `:` 去继承一个类。暂不支持多继承。

```
class A {
  printA() {
    println("a")
  }
}

class B: A {
  printA() {
    println("b")
    // 通过 `super` 关键字去调用超类方法。
    super.printA(a)
  }
	
  printB() {
    println("b")
  }
}

var b = B()
b.printA()
// will print ba
```
