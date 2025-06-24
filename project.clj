(defproject my-quest-clojure "0.1.0-SNAPSHOT"
  :description "Текстовый квест с REST API и веб-интерфейсом"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring "1.9.4"]
                 [compojure "1.6.2"]
                 [ring/ring-json "0.5.1"]
                 [org.clojure/clojurescript "1.11.60"]
                 [cljs-http "0.1.46"]]
  :plugins [[lein-cljsbuild "1.1.8"]]
  :source-paths ["src" "src-cljs"]
  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src-cljs"]
              :compiler {:output-to "resources/public/cljs-out/frontend.js"
                         :main my-quest-clojure.frontend
                         :optimizations :simple
                         :pretty-print true}}]}
  :main my-quest-clojure.server
  :clean-targets ^{:protect false} ["resources/public/cljs-out" "target"])
