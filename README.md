## __Webアプリ__ に __ClojureScript__ を入れてみました。
------------------------------

LDAP(Exchange)サーバーに接続して、表示名をメールアドレスに変換するツールを作って運用しているのですが、
チャッと作ったので、静的ページのみになっていて、結果は別ページに表示されます。

2014 Advent Calenderのネタが思いつかないうちに12月になってしまい、苦しまぎれにこれにほぼ未経験のClojureScriptを
入れてAJAXっぽくしてみることにしました。

ということで、この記事は、[2014 Clojure Advent Calender](http://qiita.com/advent-calendar/2014/clojure) の17日目の記事です。

------------------------------
## 環境
職場ではそうなのと、emacs-24.3あたりからWindowsが環境でemacsがまともに使えるようになったので、
最近は、自宅でもWindowsで開発してます。 今は、emacs-24.4です。
また、コマンド環境は、 MSYS + MinGW + GIT となっていて、emacsのeshellをshell環境として使っています。

## ソースについて
ここにあるソースは、実際に動いているものから当り障りのある部分を、主にLDAPサーバー周りで、
除外/変更したものなので、__このままじゃあ動きません。__

## 元のツール

[1.0.0 が元にするツールです。](https://github.com/ypsilon-takai/ask-mail-addr/tree/1.0.0)

LDAP(Exchange)サーバーに接続して、表示名をメールアドレスに変換するツールです。

仕事に「自動的にメールを送信するツールを作る」というのがあるのですが、職場は Outlook ベースでの作業なので、
依頼者はそこから取った宛先を送ってきます。ところがそれはメールアドレスではなく、「表示名」というものなので、
既存の環境ではそれを使って自動メール送信はできません。

また、手で表示名からメールアドレスを取得することもできますが、面倒で、僕など5つ以上やるきになりません。

ということで、こんなツールを作ってみたわけです。

![元の画面](https://github.com/ypsilon-takai/ask-mail-addr/blob/master/article/img_V100_01.png)

テキストエリアに表示名を入れてボタンを押すと、次のページに結果が出ます。

------------------------------
## ClojureScript を入れる準備

Tag 1.0.1 で行なった変更です。

### project.cljを変更
`project.clj` に`ClojureScript`を使うための設定を入れます。

- `:dependencies` に ClojurScript と domina を追加
dominaは、DOMを使いやすくするモジュールらしい。
```clojure
[org.clojure/clojurescript "0.0-2371"]
[domina "1.0.2"]
```

- `:source-paths` を追加
ここでは、Clojureの方のソースの置き場所を指定します。
src/cljとする流儀もあるようですが、階層が深くならない`src-clj``を選びました。

- `:cljsbuild` を追加
ここには、ClojureScript関連の定義を入れます。
:builds の :source-path はディレクトリ構成から `src-cljs"` になります。
また、出力するjavascriptファイルは、`:output-to`に`"resources/public/js/query.js"`と設定します。
現在の設定は最適化などが外れていますが、本格的にやるなら:optimizationなどを変更することになるようです。


設定を変更したら、`lein deps` します。

### ディレクトリの変更
トップディレクトリにある`src` ディレクトリは、Clojure のソースの格納場所なので、
`src-clj`に名前を変更します。
そして、同階層に`src-cljs`を作ります。


### 動作確認
動かして、元通りに動作することを確認します。

cider-jack-in(C-c M-j)して、REPLを立ち上げ、(ask-mail-addr.repl/start-server) を起動して動作確認します。

[ここまでの修正版は1.0.1です](https://github.com/ypsilon-takai/ask-mail-addr/tree/1.0.3)

------------------------------
## ClojureScriptを入れてみる

このあたりはTag 1.0.2

### ページをJS対応にする

ページの生成には、[hiccup](https://github.com/weavejester/hiccup) を使っています。

元ネタはIDなど設定しないでべたーっと書いてあるので、
必要な部分をDOMとして参照できるようにIDを付けたりします。

- namespaceの変更
`hiccup.pageにinclude-js`を追加します。 javascriptのinclude文に展開されます。
```clojure
[hiccup.page :refer [html5 include-css include-js]]
```

- formにIDを付けます。
```clojure
(form-to {:id "main-form"}
    [:post "/mail-addr/ask"]
```
		 
- 結果を出力する場所を作ります。
別ページに遷移して表示していた結果を、同一ページ内に表示するための場所を作ります。
```clojure
     [:div
      [:p {:id "result-area"
           :name "result-area"}
      "Reselt will be here"]
     (text-area {:id "result-area"
                 :style "width:300px; height:150px;"}
                :result-area)]
    (include-js "public/js/query.js")]))
```

ここまで書くと、見た目はこんなふうになります。

[これが1.0.2です](https://github.com/ypsilon-takai/ask-mail-addr/tree/1.0.3)

![出力エリア追加状態の図](https://github.com/ypsilon-takai/ask-mail-addr/blob/master/article/img_V102_01.png)

### ClojureScript を書く
`src-cjls`ディレクトリに、cjlsファイルを作成します。ファイル名は`get_responce.cljs`としました。

Webを巡ってみると、[こんなページを見つけました](https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md)
これを参考にしてコードを書いてみます。

ns の内容は以下の通り。
```clojure
(ns get-responce
  (:require [goog.net.XhrIo :as xhr]
            [domina :as dom]
            [domina.events :as devt]))
```

DOMモジュールの [domina](https://github.com/levand/domina) の定義と、POSTメッセージを投げるために `goog.net.XhrIo`を使うことにしました。

Webページとか本から情報を漁りながらやっているので、まだ使わない関数も入ってますが、使うのは、以下のところです。
まずは、Submitボタンを押すと、表示エリアに IT WORKS! と表示させてみようとしています。
```clojure
(defn test-func []
  (let [text-area (dom/by-id "result-area")]
    (dom/set-value! text-area "IT WORKS!")))

(defn init []
  (if (and js/document
           (.-getElementById js/document))
    (let [main-form (.getElementById js/document "main-form")]
       (set! (.-onsubmit main-form) test-func))))

(set! (.-onload js/window) init)
```

最後のところで、windowそのもののonloadイベントに、init関数を設定しています。
この処理は、このjsファイルがロードされたときに実行されます。

init関数では、js/documentがあれば、`main-form` フォームのonsubmitイベントに、test-func関数を設定します。

test-funcは、呼ばれるとresult-areaという名前のtext-areaのvalueを"IT WORKS!"に書きかえます。

### ClojureScript を javascript にコンパイル
leiningenのモジュールの cljsbuild を使ってjavascriptにコンパイルします。
```
lein cljsbuild once
```
ここでonceでなくてautoを指定すると、cljsファイルを保存するたびに自動でコンパイルしてくれるようなのですが、比較的小まめに保存するたちなのと、慣れていないのでいつでもちゃんとしたソースが書けるわけではないので、とりあえず、 明示的に コンパイルすることにします。

コンパイルが終ったので、`resources/public/js`ディレクトリを見てみると、ちゃんと、`query.js`ファイルができています。 かなり大きなファイルになってますが、最後の方に、定義したtest-fnやinitが入っているの見えます。

### さて、実行してみます。
REPLから`(start-server)`すると、ちゃんと思ったとおりの画面が表示されました。

適当な名前を入力して、submitボタンを押します。「IT WORKS!」と表示されます。

ところが、なんと、次の瞬間、画面が遷移して結果が表示されてしまいます。
思った通りに`test-func`は呼ばれたのは成功ですが、もともとのsubmitボタンのデフォルトの動作を上書きしてくれているわけではなく、両方有効になってしまっているようです。


------------------------------
## JvaScriptの作法 1

今回ほぼ初めて`JavaScript(ClojureScript)`を触っているので、これをどのように解決すべきなのかよくわかりません。
でも、たぶん

1) 現状の構成のままデフォルトの動作を止める。
2) デフォルトの動作はフォームに入っているので、サブミットボタンにしない。

のどちらかかな。

参考にしたページのコードを改めてよく読んでみます。

`submit`ボタンを押したときに呼び出されるコードは、これです。

```clojure
(defn validate-form []
  ;; get email and password element using (by-id id)
  (let [email (by-id "email")
        password (by-id "password")]
    ;; get email and password value using (value el)
    (if (and (> (count (value email)) 0)
             (> (count (value password)) 0))
      true
      (do (js/alert "Please, complete the form!")
          false))))
```

これはログインフォームなので、`true`になると、次の画面に遷移するのですが、
情報が正しく入力されていれば`true`を、そうでなければ`false`を
返すとういう動作です。
ということは、次の画面に遷移するという動作を、返り値で制御しているということ
のようです。なるほど。

やってみるとちゃんと動きます。

```clojure
(defn test-func []
  (let [text-area (dom/by-id "result-area")]
    (dom/set-value! text-area "IT WORKS!"))
    false)
```

これはこれでよさそうですが、裏口みたいでちゃんと制御している気がしませんね。
ClojureScriptは書いたことはないんですが、すでに本は読んでます。
[ClojureScript Up and Running](http://shop.oreilly.com/product/0636920025139.do)

改めてこれを見かえしてみると、1の方法を取っています。

```clojure
(defn ^:export main []
  (events/listen! (d/by-id button-id)
                  :click
                  (fn [event]
                    (post-for-eval (get-expr))
                    (events/stop-propagation event)
                    (events/prevent-default event))))
```

この最後のやつがたぶんそれでしょう。発生したイベントに反応するブラウザの
デフォルト動作を止める関数のようです。

また、この方式は、元からあるイベントを書き換えるのではなく、
イベントリスナーにイベントを登録する作法です。
多くのイベントを制御するのであれば、こちらの方が管理が楽かもしれません。


------------------------------
## コードの修正

コード修正します。

ついでに、lein cljsbuild を実行したときに出た warning のところも修正しておきます。
`:builds`のところがベクタになってます。
- 複数のターゲット`.js`ファイルを指定することができる。
- idを付けると、そのidだけターゲットにすることができる。  
ということは、たとえば、開発用とリリース用にコンパイルオプションを変えておく
などということもできるということ。

__前__
```clojure
  :cljsbuild {:builds
              {:source-paths ["src-cljs"] 
                :compiler {:output-to "resources/public/js/query.js"
                           :optimization :whitespace
                           :pretty-print true}}}
```

__後__
```clojure
  :cljsbuild {:builds
              [{:source-paths ["src-cljs"] 
                :compiler {:output-to "resources/public/js/query.js"
                           :optimization :whitespace
                           :pretty-print true}}]}
```

[ここまでの修正版は1.0.3です](https://github.com/ypsilon-takai/ask-mail-addr/tree/1.0.3)

![IT WORKS!](https://github.com/ypsilon-takai/ask-mail-addr/blob/master/article/img_V102_02.png)
------------------------------
## LDAPの情報を取得するようにする

ClojureScriputで情報を表示できるようになったので、LDAPで取得したデータを
表示するようにしてみます。

ところが、これが、なかなか、うまくいかなくて、いろいろ試したりしたのですが、
記録が残ってません。 最終結果を元に覚えている範囲で書いてみます。

### データ送信処理

`test-func`の代わりに`submit-info`を書きます。

```clojure
(defn submit-info []
  (let [input-data (dom/value (dom/by-id "user-list"))
        param-data (doto (js/FormData.)
                (.append "user-list" input-data))]
    (POST "/mail-addr/ask"
          {:params param-data
           :handler receiver
           :responce-format :edn})
    false))
```

この関数で、入力テキストエリアにある文字列を、サーバーに送って、結果を受け取る関数を
登録しています。
前のデータに残っているように、最初は送信部分を`goog.net.XhrIo.send()`でやろうと
思っていたのですが、うまく受け取れません。 解決してみれば、サーバー側で「テキスト」データ
として扱えばよかったということのようなのですが、解決策としては、

* `cljs-ajax`ライブラリのPOST関数を使う。[ここ](https://github.com/JulianBirch/cljs-ajax)
* 送信するデータは、`js/FormData`オブジェクトを作って送る。

ということになってます。

### サーバー処理
ページからのデータを受け取ったサーバーは、LDAPに問い合わせます。
ルートは変更してませんが、応答内容は、ページに出力用に整形されたテキストでなく、
`edn`形式で送信します。

__前__
```clojure
(defn get-maddr [user-list]
  (print-result (ldap/ask-all-email-address user-list)))
  ```
__後__
```clojure
(defn get-maddr-edn [user-list]
  (str (vec (ldap/ask-all-email-address user-list))))
```

データを`vec`して`str`して返していますが、こうしないと動きませんです。
あ、`str`は不要かもしれませんが。

この関数で返した値を、応答として返します。内容は、
* :displayName と :emailAddress のあるハッシュのリスト
です。


### 受信
受信の処理は、こんな感じです。

```clojure
(defn disp-name-and-addr [infos]
  (->> (for [i infos]
         (str (:displayName i) "<" (:emailAddress i) ">"))
       (clojure.string/join "\n" ,,)))

(defn receiver [result]
  (let [text-area (dom/by-id "result-area")
        responce (cljs.reader/read-string result)
        disp-data (disp-name-and-addr responce)]
    (dom/set-value! text-area disp-data)))
```

コールバック関数`receiver`が受け取ったデータを`disp-name-and-addr`に渡して、
返ってきたテキストを表示テキストエリアに書き込みます。

`edn`文字列からデータへの変換は、`cljs.reader/read-string`を使います。

データを表示文字列に変換する関数`disp-name-and-addr`はひっかかったところで、
もともとこんなふうに書いてました。

```clojure
(defn disp-name-and-addr [infos]
  (->> (for [{name :displayName addr :emailAddress}] infos]
         (str name "<" addr ">"))
       (clojure.string/join "\n" ,,)))
```

これが動かない。 なんで？

### 動きました。
さて、とりあえず、動きましたので、ここでおしまいです。

[ソース](https://github.com/ypsilon-takai/ask-mail-addr/tree/2.0.0)


## JavaScriptの作法 2
### ビルド
最初、`lein cljsbuild once`を使ってましたが、起動のたびにやたらに時間が
かかるので、すぐに`lein cljsbuild auto`に切り替えてしまいました。

ソースを変更するたびに、すぐにコンパイルしてくれるので便利です。

が、

現状、実行を止めるのが面倒です。
`shell`として`eshell`を使っているのですが、`C-c C-c`を送っても、止ってくれません。
結局、`(process-list)`してみつけたやつを`(kill-process)`してるんですが、
いい方法無いんですかね？

### データの送受信
現状、元の処理を踏襲して、送信するデータを`Form`にして送っているわけですが、
そもそもこういう処理をするときにどのようにすべきかよくわかってません。

Webを探してもあまり情報がみつかりません。

`JavaScript`の勉強をしないといけないのかなぁ。

## まとめと今後
ツール的にはとりあえず動いたので、あとは、消去ボタンを付けたり、
表示形式を変えたりなど、使い勝手の向上を少しやる予定です。

あとは、見た目ですかね。CSS入れたり。

最近業務でWebで情報を提供することが増えてきて、`ClojureScript`使ってみたわけですが、
知識が足りなくて苦労したところとかありはしても、以前ちょっと触った`JavaScript`よりは
100倍書きやすい(当社比)ので、これからどんどん使っていこうかと。


