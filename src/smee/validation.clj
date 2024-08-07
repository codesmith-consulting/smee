(ns smee.validation
  (:require [jkkramer.verily :as v]
            [smee.utils :as utils]
            [clojure.string :as string]
            [smee.error :refer [raise]]))

(defn fmt-validation [result]
  (let [{:keys [keys msg]} result]
    (map #(vector % (str (utils/humanize %) " " msg)) keys)))

(defn fmt-validations [results]
  (when (some? results)
    (->> (map fmt-validation results)
         (mapcat identity)
         (into {}))))

(defn validate
  "Validate the map `m` with a vector of rules `validations`,
  or a validation fn, such as that created with verily's
  `combine`.

  For example:
  ```
  (validate {:customer/id 123
             :customer/email \"sean@example.com\"}
            [[:required [:customer/id :customer/email]]
             [:email [:customer/email]]])
  ;; => {:customer/id 123
         :customer/email \"sean@example.com\"}

  (validate {} [[:required [:customer/id] \"can't be blank\"]])
  ;; => Unhandled clojure.lang.ExceptionInfo
  ;;    Invalid data: :customer/id
  ;;    {:type :invalid,
  ;;     :errors #:customer{:id \"Id can't be blank\"},
  ;;     :smee.validation/error :validation,
  ;;     :smee.error/raise true}
  ```

  See [Validator](https://coastonclojure.com/docs/validator.md) for more.
  "
  [m validations]
  (let [errors (fmt-validations
                 (if (fn? validations)
                   (validations m)
                   (v/validate m validations)))]
    (if (empty? errors)
      m
      (raise (str "Invalid data: " (string/join ", " (keys errors)))
             {:type   :invalid
              :errors errors
              ::error :validation}))))
