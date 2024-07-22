(ns smee
  (:require [smee.potemkin.namespaces :as namespaces]
            [hiccup2.core]
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
            [smee.validation])
  (:refer-clojure :exclude [update]))

(namespaces/import-vars
  [smee.responses
   ok
   bad-request
   no-content
   not-found
   unauthorized
   server-error
   redirect
   flash]

  [smee.error
   raise
   rescue]

  [smee.db
   q
   pull
   transact
   delete
   insert
   update
   first!
   pluck
   fetch
   execute!
   find-by
   transaction
   upsert
   any-rows?]

  [smee.db.connection
   connection]

  [smee.validation
   validate]

  [smee.components
   csrf
   form
   js
   css]

  [smee.router
   routes
   wrap-routes
   prefix-routes
   with
   with-prefix]

  [smee.middleware
   wrap-with-layout
   with-layout
   wrap-layout
   site-routes
   site
   api-routes
   api
   content-type?]

  [smee.theta
   server
   app
   url-for
   action-for
   redirect-to
   form-for]

  [smee.env
   env]

  [smee.utils
   uuid
   intern-var
   xhr?]

  [smee.time2
   now
   datetime
   instant
   strftime]

  [hiccup2.core
   raw
   html])
