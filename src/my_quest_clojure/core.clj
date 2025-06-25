(ns my-quest-clojure.core)

(defrecord Player [scene inventory])
(defrecord Scene [description actions])
(defrecord Action [description required-item gives-item next-scene])

(def game-scenario
  {1 (->Scene
       "Вы проснулись в лесу у подножия старой горы. Рядом лежит крепкая палка, которая может пригодиться."
       [(->Action "Взять палку" nil :палка 2)])
   2 (->Scene
       "Перед вами тропинка раздваивается. Справа слышен шум реки, слева густой лес."
       [(->Action "Пойти к реке" nil nil 3)
        (->Action "Пойти в лес" nil nil 4)])
   3 (->Scene
       "У реки вы нашли заброшенную лодку. Вода кажется холодной и быстрой."
       [(->Action "Взять лодку" nil :лодка 3)
        (->Action "Переплыть реку" :лодка nil 5)
        (->Action "Вернуться к развилке" nil nil 2)])
   4 (->Scene
       "Вы углубились в лес и обнаружили древнюю хижину с запертой дверью."
       [(->Action "Осмотреться вокруг" nil nil 6)
        (->Action "Вернуться к развилке" nil nil 2)
        (->Action "Попробовать открыть дверь" :ключ nil 7)])
   5 (->Scene
       "Вы переплыли реку и оказались у маленькой деревни. Вы спасены!"
       [])
   6 (->Scene
       "Осмотрев окрестности, вы нашли старый ржавый ключ, лежащий под камнем."
       [(->Action "Взять ключ" nil :ключ 4)
        (->Action "Вернуться в хижину" nil nil 4)])
   7 (->Scene
       "Дверь открылась, и перед вами предстает мудрый старец. Он помогает вам выбраться из леса. Конец истории."
       [])})

(def player (atom (->Player 1 #{})))

(defn available-actions [player scenario]
  (let [{:keys [scene inventory]} player
        {:keys [actions]} (get scenario scene)]
    (filter #(and
               (or (nil? (:required-item %))
                   (contains? inventory (:required-item %)))
               (not (and (:gives-item %)
                         (contains? inventory (:gives-item %)))))
            actions)))

(defn scene-data [player scenario]
  (let [{:keys [scene]} player
        {:keys [description]} (get scenario scene)
        actions (available-actions player scenario)
        indexed-actions (mapv (fn [a idx]
                                {:idx idx
                                 :description (:description a)})
                              actions (range 1 (inc (count actions))))]
    (println "scene-data:" {:scene scene :actions indexed-actions})
    {:scene scene
     :description description
     :actions indexed-actions}))

(defn safe-parse-int [s]
  (try
    (Integer/parseInt s)
    (catch Exception _ 
      (println "safe-parse-int: failed to parse" s)
      nil)))

(defn update-player-state [player scenario choice]
  (println "update-player-state called with choice:" choice)
  (let [{:keys [scene inventory]} player
        actions (available-actions player scenario)
        choice-num (if (string? choice)
                     (do
                       (println "Parsing string choice:" choice)
                       (safe-parse-int choice))
                     choice)]
    (println "Parsed choice-num:" choice-num)
    (let [idx (when (number? choice-num)
                (dec choice-num))]
      (if (and (some? idx)
               (>= idx 0)
               (< idx (count actions)))
        (let [action (nth actions idx)]
          (println "Selected action:" action)
          (let [new-inv (if (:gives-item action)
                          (conj inventory (:gives-item action))
                          inventory)
                new-scene (:next-scene action)]
            (println "Updating player state to scene:" new-scene "with inventory:" new-inv)
            (->Player new-scene new-inv)))
        (do
          (println "Invalid choice: index out of bounds or parse error. idx:" idx ", actions available:" (count actions))
          player)))))

