(ns my-quest-clojure.frontend
  (:require [cljs.core.async :refer [<!]]
            [cljs-http.client :as http]
            [goog.dom :as gdom])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn inject-css []
  (let [style (.createElement js/document "style")]
    (set! (.-textContent style)
          "body {
             font-family: Arial, sans-serif;
             background-color: #f0f0f0;
             margin: 20px;
           }
           #app {
             max-width: 600px;
             margin: auto;
             background: white;
             padding: 20px;
             border-radius: 8px;
             box-shadow: 0 0 8px rgba(0,0,0,0.1);
           }
           form label {
             display: block;
             margin-bottom: 10px;
             cursor: pointer;
           }
           button {
             padding: 8px 12px;
             font-size: 1em;
             cursor: pointer;
             border-radius: 4px;
             border: 1px solid #ccc;
             background-color: #eee;
           }
           button:hover {
             background-color: #ddd;
           }")
    (.appendChild (.-head js/document) style)))

(defn render-scene [{:keys [description actions]}]
  (let [root (gdom/getElement "app")]
    (set! (.-innerHTML root) "")
    (let [desc-el (.createElement js/document "p")]
      (set! (.-textContent desc-el) description)
      (.appendChild root desc-el))
    (if (empty? actions)
      (let [end-el (.createElement js/document "p")]
        (set! (.-textContent end-el) "Игра окончена. Спасибо за игру!")
        (.appendChild root end-el))
      (let [form (.createElement js/document "form")]
        (set! (.-id form) "choice-form")
        (doseq [{:keys [idx description]} actions]
          (let [label (.createElement js/document "label")
                radio (.createElement js/document "input")
                br (.createElement js/document "br")]
            (set! (.-type radio) "radio")
            (set! (.-name radio) "choice")
            (set! (.-value radio) (str idx))
            (when (= idx 1)
              (set! (.-checked radio) true))
            (.appendChild label radio)
            (.appendChild label (.createTextNode js/document (str " " description)))
            (.appendChild form label)
            (.appendChild form br)))
        (let [submit (.createElement js/document "button")]
          (set! (.-type submit) "submit")
          (set! (.-textContent submit) "Выбрать")
          (.appendChild form submit))
        (.appendChild root form)
        (.addEventListener form "submit"
                           (fn [e]
                             (.preventDefault e)
                             (let [checked-input (.querySelector form "input[name=choice]:checked")
                                   choice (when checked-input (.-value checked-input))
                                   choice-int (js/parseInt choice)]
                               (js/console.log "Selected choice (int):" choice-int)
                               (if choice
                                 (go
                                   (let [resp (<! (http/post "/step"
                                                             {:json-params {:choice choice-int}}))
                                         data (:body resp)]
                                     (js/console.log "Response from server:"
                                                     (js/JSON.stringify (clj->js data) nil 2))
                                     (render-scene data)))
                                 (js/alert "Пожалуйста, выберите вариант!")))))))))

(defn ^:export start []
  (inject-css)
  (go
    (let [resp (<! (http/get "/scene" {:with-credentials? false}))
          data (:body resp)]
      (render-scene data))))
