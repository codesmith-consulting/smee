(ns smee.generators
  (:require [smee.generators.code :as generators.code]
            [smee.generators.migration :as generators.migration]
            [smee.migrations :as migrations]
            [smee.db :as db]))


(defn usage []
  (println "Usage:
  smee new <project-name>
  smee gen migration <name>
  smee gen code <table>
  smee db <migrate|rollback>

Examples:
  smee new foo
  smee new another-foo

  smee gen migration create-table-todo     # Creates a new migration file
  smee gen sql:migration create-table-todo # Creates a new sql migration file

  smee gen code todo                       # Creates a new clj file with handler functions in src/todo.clj

  smee db migrate                          # runs all migrations found in db/migrations
  smee db rollback                         # rolls back the latest migration"))


(defn gen [args]
  (let [[_ kind arg] args]
    (case kind
      "migration" (generators.migration/write (drop 2 args))
      "code" (generators.code/write arg)
      (usage))))


(defn -main [& args]
  (let [[action] args]
    (case action
      "gen" (gen args)
      "db" (cond
             (contains? #{"migrate" "rollback"} (second args)) (migrations/-main (second args))
             (contains? #{"create" "drop"} (second args)) (db/-main (second args))
             :else (usage))
      (usage))))
