# Excavator Tools
## これなに
いわゆる 3x3 の範囲破壊が出来るツールを追加する Spigot 用のプラグインです。

## (動作|作成)環境
- Spigot 1.18
- Java 17
- Kotlin 1.6.0

## How to build
以下のコマンドを入力すると、build/libs/以下にjarファイルができあがります。

```bash
$ ./gradlew jar
```

## 使い方

`x` を元になった素材（各種木材 / 丸石 / etc）、`o` をツルハシかシャベルとして、作業台でクラフトしてください。

|   |   |   |
| - | - | - |
|   | x |   |
| x | o | x |
|   | x |   |

このツールを使うと、身体のある向きに対して 3x3 の範囲で一括採掘が行われます。

## 注意
壊した面に対してではなく、身体のある場所と破壊したブロックの位置関係で破壊面が決まるので、想定外の面が削れることがありますが、今のところ仕様です。

## ライセンス
MIT とします。
