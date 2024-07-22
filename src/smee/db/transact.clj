(ns smee.db.transact
  (:require [smee.utils :as utils]
            [clojure.string :as string]
            [clojure.set]
            [smee.db.schema])
  (:refer-clojure :exclude [ident? update]))

(defn ident? [schema val]
  (and
    (vector? val)
    (contains? (:idents schema) (first val))))

(defn same-ns? [m]
  (and (map? m)
       (= 1 (->> m keys (map namespace) (distinct) (count)))))

(defn qualified-map? [m]
  (and (map? m)
       (not (empty? m))
       (every? qualified-ident? (keys m))))

(defn col
  ([table val]
   (when (clojure.core/ident? val)
     (let [prefix (if (nil? table)
                    ""
                    (str table "."))]
       (->> val name utils/snake
            (str prefix)))))
  ([val]
   (when (qualified-ident? val)
     (let [k-ns (-> val namespace utils/snake)
           k-n (-> val name utils/snake)]
       (str k-ns "." k-n " as " k-ns "$" k-n)))))

(defn ? [m]
  (->> (keys m)
       (map (fn [_] (str "?")))
       (string/join ", ")))

(defn validate-transaction [m]
  (cond
    (not (same-ns? m)) (throw (Exception. "All keys must have the same namespace"))
    (not (qualified-map? m)) (throw (Exception. "All keys must be qualified"))
    :else m))


(defn insert-into [schema m]
  (let [table (-> m keys first namespace utils/snake)
        rm (clojure.set/rename-keys m (:joins schema))
        s (->> (keys rm)
               (map #(col nil %))
               (string/join ", ")
               (utils/surround "()"))]
    (str "insert into " table s)))

(defn values [v]
  (str "values " (->> (map ? v) (map #(utils/surround "()" %))
                      (string/join ", "))))

(defn select-col [schema [k v]]
  (if (ident? schema v)
    (str (-> k name (string/replace #"-" "."))
         " as " (-> k name (string/replace #"-" "$")))
    (col k)))

(defn select [schema m]
  (let [s (->> (clojure.set/rename-keys m (:joins schema))
               (map (partial select-col schema))
               (string/join ", "))]
    (str "select " s)))

(defn ident->str [val sep]
  (when (qualified-ident? val)
    (let [k-ns (-> val namespace utils/snake)
          k-n (-> val name utils/snake)]
      (str k-ns sep k-n))))

(defn from-col [schema [k v]]
  (if (ident? schema v)
    (ident->str (first v) "_")
    (col nil k)))

(defn from [schema m]
  (let [table (-> m keys first namespace utils/snake)
        rm (clojure.set/rename-keys m (:joins schema))
        vals-str (->> (? rm) (utils/surround "()"))
        cols-str (->> (map #(from-col schema %) rm) (string/join ", "))]
    (str "from (values " vals-str ") as " table "(" cols-str ")")))

(defn join [[k v]]
  (let [table (-> k namespace utils/snake)]
    (str "join "
         (-> k name utils/snake)
         " on "
         (ident->str (first v) ".")
         " = "
         table "." (ident->str (first v) "_"))))

(defn joins [schema m]
  (let [idents (->> (filter (fn [[_ v]] (ident? schema v)) m)
                    (into {}))]
    (when (not (empty? idents))
      (->> (map join idents)
           (string/join ", ")))))

(defn on-conflict [schema m]
  (let [table (-> m keys first namespace utils/snake)
        rm (clojure.set/rename-keys m (:joins schema))
        idents (->> (get schema :idents)
                    (filter #(= table (namespace %)))
                    (filter #(not= % (keyword table "id"))))
        cols-str (if (or (contains? m (keyword table "id"))
                         (empty? idents))
                   "id"
                   (->> (map #(col nil %) idents)
                        (string/join ",")))
        excluded-cols-str (->> (map #(str (col nil %) " = " (if (= % :updated-at) "now()" (col "excluded" %)))
                                    (-> (apply dissoc rm idents)
                                        (keys)
                                        (conj :updated-at)))
                               (string/join ", "))]
    (str " on conflict (" cols-str ") do update set " excluded-cols-str)))

(defn sql-map [schema v]
  {:insert-into (insert-into schema (first v))
   :values (values v)
   :on-conflict (on-conflict schema (first v))})

(defn ident-val [val]
  (if (vector? val)
    (second val)
    val))

(defn selects [m]
    (mapv (fn [[k v]]
           [(str "select " (name k) ".id as " (namespace k) "$" (name k) " from " (name k) " where " (-> v first name) " = ?") (second v)])
          m))

(defn single [v]
  (if (= 1 (count v))
    (first v)
    v))

(defn sql-vec [arg]
  (let [v (if (map? arg) [arg] arg)
        schema (smee.db.schema/fetch)
        _ (map validate-transaction v)
        maps (map #(into (sorted-map) %) v)
        returning "returning *"
        {:keys [insert-into values on-conflict]} (sql-map schema maps)
        sql (->> [insert-into values on-conflict returning]
                 (filter some?)
                 (string/join "\n"))
        params (->> (map vals maps)
                    (mapcat identity)
                    (map ident-val))]
    (apply conj [sql] params)))
