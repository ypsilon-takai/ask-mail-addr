## しょぼい __Webアプリ__ にしょぼい __ClojureScript__ を入れてみました。
------------------------------

LDAP(Exchange)サーバーに接続して、表示名をメールアドレスに変換するツールを作って運用しているのですが、
チャッと作ったので、静的ページのみになっていて、結果は別ページに表示されます。

2014 Advent Calenderのネタが思いつかないうちに12月になってしまい、苦しまぎれにこれにほぼ未経験のClojureScriptを
入れてAJAXっぽくしてみることにしました。

ということで、この記事は、[2014 Clojure Advent Calender]() の17日目の記事です。

------------------------------
## 環境
職場ではそうなのと、emacs-24.3あたりからWindowsが環境でemacsがまともに使えるようになったので、
最近は、自宅でもWindowsで開発してます。 今は、emacs-24.4です。
また、コマンド環境は、 MSYS + MinGW + GIT となっていて、emacsのeshellをshell環境として使っています。


## 元のツール

TAG 1.0.0 が元にするツールです。

LDAP(Exchange)サーバーに接続して、表示名をメールアドレスに変換するツールです。

仕事に「自動的にメールを送信するツールを作る」というのがあるのですが、職場は Outlook ベースでの作業なので、
依頼者はそこから取った宛先を送ってきます。ところがそれはメールアドレスではなく、「表示名」というものなので、
既存の環境ではそれを使って自動メール送信はできません。

また、手で表示名からメールアドレスを取得することもできますが、面倒で、僕など5つ以上やるきになりません。

ということで、こんなツールを作ってみたわけです。

[元の画面](https://raw.githubusercontent.com/clojure-emacs/cider/master/logo/cider-logo-w640.png)

テキストエリアに表示名を入れてボタンを押すと、次のページに結果が出ます。

------------------------------
## ClojureScript を入れる準備

Tag 1.0.1 で行なった変更です。

### project.cljを変更
`project.clj` に`ClojureScript`を使うための設定を入れます。

- `:dependencies` に ClojurScript と domina を追加
dominaは、DOMを使いやすくするモジュールらしい。
```
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

------------------------------
## ClojureScriptを入れてみる

このあたりはTag 1.0.2

### ページをJS対応にする

ページの生成には、[hiccup]() を使っています。べたーっとIDなど設定しないで書いてあるので、
必要な部分をDOMとして参照できるようにIDを付けたりします。

- namespaceの変更
`hiccup.pageにinclude-js`を追加します。 javascriptのinclude文に展開されます。
```
[hiccup.page :refer [html5 include-css include-js]]
```

- formにIDを付けます。
```
(form-to {:id "main-form"}
    [:post "/mail-addr/ask"]
```
		 
- 結果を出力する場所を作ります。
別ページに遷移して表示していた結果を、同一ページ内に表示するための場所を作ります。
```
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

[出力エリア追加](https://raw.githubusercontent.com/clojure-emacs/cider/master/logo/cider-logo-w640.png)

### ClojureScript を書く
`src-cjls`ディレクトリに、cjlsファイルを作成します。ファイル名は`get_responce.cljs`としました。

Webを巡ってみると、[こんなページを見つけました](https://github.com/magomimmo/modern-cljs/blob/master/doc/tutorial-05.md)
これを参考にしてコードを書いてみます。

ns の内容は以下の通り。
```
(ns get-responce
  (:require [goog.net.XhrIo :as xhr]
            [domina :as dom]
            [domina.events :as devt]))
```
DOMモジュールの domina の定義と、POSTメッセージを投げるために `goog.net.XhrIo`を使うことにしました。

Webページとか本から情報を漁りながらやっているので、まだ使わない関数も入ってますが、使うのは、以下のところです。
まずは、Submitボタンを押すと、表示エリアに IT WORKS! と表示させてみようとしています。
```
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

適当な名前を入力して、submitボタンを押します。

「IT WORKS!」と表示されますが、なんと、次の瞬間、画面が遷移して結果が表示されてしまいます。
思った通りに`test-func`は呼ばれたのは成功ですが、もともとのsubmitボタンのデフォルトの動作を上書きしてくれているわけではなく、両方有効になってしまっているようです。


------------------------------
## JvaScriptの作法 1

今回ほぼ初めてJavaScript(ClojureScript)を触っているので、これをどのように解決すべきなのかよくわかりません。
でも、たぶん

1) 現状の構成のままデフォルトの動作を止める。
2) デフォルトの動作はフォームに入っているので、サブミットボタンにしない。

のどちらかかな。

参考にしたページのコードを改めてよく読んでみます。

`submit`ボタンを押したときに呼び出されるコードは、これです。

```
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
です。なるほど。

やってみるとちゃんと動きます。

```
(defn test-func []
  (let [text-area (dom/by-id "result-area")]
    (dom/set-value! text-area "IT WORKS!"))
    false)
```

これはこれでよさそうですが、ちゃんと制御している気がしませんね。
ClojureScriptは書いたことはなくても、本は読んでます。
[ClojureScript Up and Running](http://shop.oreilly.com/product/0636920025139.do)
これを見ると、1の方法を取っています。

```
(defn ^:export main []
  (events/listen! (d/by-id button-id)
                  :click
                  (fn [event]
                    (post-for-eval (get-expr))
                    (events/stop-propagation event)
                    (events/prevent-default event))))
```

この最後のやつがたぶんそれでしょう。発生したイベントに反応するブラウザの
動作を止める関数のようです。
また、この方式は、イベントリスナーにイベントを登録する作法です。
多くのイベントを制御するのであれば、こちらの方が管理が楽かもしれません。


------------------------------
## コードの修正

コード修正します。

ついでに、lein cljsbuild を実行したときに出た warning のところも修正しておきます。

[修正板は1.0.3です]()

------------------------------
## LDAPの情報を取得するようにする
















