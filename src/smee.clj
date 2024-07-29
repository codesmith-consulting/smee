(ns smee
  (:require [hiccup2.core]
            [smee.db]
            [smee.db.connection]
            [smee.theta]
            [smee.env]
            [smee.time2]
            [smee.components]
            [smee.responses]
            [smee.utils]
            [smee.error]
            [smee.router]
            [smee.validation]
            [smee.middleware :as middleware])
  (:refer-clojure :exclude [update]))

;; TODO(Mike)
;; 1. Ensure tests for this namespace which forms the public api of smee
;; 2. Redefing like this allows jump to code definiton but loses doc meta data. Add this doc metadata back in
;; 3. Kill old time library, see Sean's old changes https://github.com/coast-framework/coast/compare/master...next 

(def ok smee.responses/ok)
(def bad-request smee.responses/bad-request)
(def no-content smee.responses/no-content)
(def not-found smee.responses/not-found)
(def unauthorized smee.responses/unauthorized)
(def server-error smee.responses/server-error)
(def redirect smee.responses/redirect)
(def flash smee.responses/flash)

(def raise smee.error/raise)
(defmacro rescue [& args] `(smee.error/rescue ~@args))

(def q smee.db/q)
(def pull smee.db/pull)
(def transact smee.db/transact)
(def delete smee.db/delete)
(def insert smee.db/insert)
(def update smee.db/update)
(def first! smee.db/first!)
(def pluck smee.db/pluck)
(def fetch smee.db/fetch)
(def execute! smee.db/execute!)
(def find-by smee.db/find-by)
(def upsert smee.db/upsert)
(def any-rows? smee.db/any-rows?)
(defmacro transaction [& args] `(smee.db/transaction ~@args))

(def connection smee.db.connection/connection)

(def validate smee.validation/validate)

(def csrf smee.components/csrf)
(def form smee.components/form)
(def js smee.components/js)
(def css smee.components/css)

(def routes smee.router/routes)
(def wrap-routes smee.router/wrap-routes)
(def prefix-routes smee.router/prefix-routes)
(def with smee.router/with)
(def with-prefix smee.router/with-prefix)

(def wrap-with-layout smee.middleware/wrap-with-layout)
(def with-layout smee.middleware/with-layout)
(def wrap-layout smee.middleware/wrap-layout)
(def site-routes smee.middleware/site-routes)
(def site smee.middleware/site)
(def api-routes smee.middleware/api-routes)
(def api smee.middleware/api)
(def content-type? smee.middleware/content-type?)

(def server smee.theta/server)
(def app smee.theta/app)
(def url-for smee.theta/url-for)
(def action-for smee.theta/action-for)
(def redirect-to smee.theta/redirect-to)
(def form-for smee.theta/form-for)

(def env smee.env/env)

(def uuid smee.utils/uuid)
(def intern-var smee.utils/intern-var)
(def xhr? smee.utils/xhr?)

(def now smee.time2/now)
(def datetime smee.time2/datetime)
(def instant smee.time2/instant)
(def strftime smee.time2/strftime)

(def raw hiccup2.core/raw)
(defmacro html [& args] `(hiccup2.core/html ~@args))
