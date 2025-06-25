(ns my-quest-clojure.server
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]  
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.util.response :refer [response resource-response content-type]]
   [my-quest-clojure.core :refer [game-scenario player update-player-state]] 
   [clojure.pprint :refer [pprint]])
  
  (:gen-class))

;; Безопасный парсер int из строки
(defn safe-parse-int [s]
  (try
    (Integer/parseInt s)
    (catch Exception _ nil)))

;; Игровое состояние
(defonce player-state (atom @player))

;; Генерация данных текущей сцены для клиента
(defn scene-data []
  (let [{:keys [scene inventory]} @player-state
        {:keys [description actions]} (get game-scenario scene)
        available-actions (filter #(and
                                    (or (nil? (:required-item %))
                                        (contains? inventory (:required-item %)))
                                    (not (and (:gives-item %)
                                              (contains? inventory (:gives-item %)))))
                                  actions)]
    {:scene scene
     :description description
     ;; Исправлено: mapv теперь получает две коллекции, разделённые пробелом
     :actions (mapv (fn [a idx]
                      {:idx idx
                       :description (:description a)})
                    available-actions
                    (range 1 (inc (count available-actions))))}))

;; Роуты приложения
(defroutes app-routes
  ;; Главная страница
  (GET "/" []
    (-> (resource-response "public/index.html")
        (content-type "text/html")))

  ;; Получить данные сцены
  (GET "/scene" []
    (response (scene-data)))

  ;; Обработать выбор игрока
  (POST "/step" {body :body headers :headers}
    (println "=== /step Request ===")
    (println "Headers:\n" (with-out-str (clojure.pprint/pprint headers)))
    (println "Body params:\n" (with-out-str (clojure.pprint/pprint body)))
    (let [choice (some-> body :choice safe-parse-int)]
      (println "Parsed choice:" choice)
      (if (nil? choice)
        (do
          (println "Choice is nil or invalid, returning error.")
          (response {:error "Invalid choice"}))
        (do
          (swap! player-state update-player-state game-scenario choice)
          (println "Updated player state:\n" (with-out-str (clojure.pprint/pprint @player-state)))
          (println "=====================")
          (response (scene-data))))))


  (route/resources "/")

  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (wrap-json-body {:keywords? true})
      wrap-params
      wrap-json-response))

;; Точка входа
(defn -main []
  (println "Starting server on http://localhost:3000")
  (run-jetty app {:port 3000 :join? false}))
