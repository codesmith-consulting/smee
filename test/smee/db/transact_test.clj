(ns smee.db.transact-test
  (:require [smee.db.transact :as transact]
            [smee.db.schema]
            [clojure.test :refer [deftest testing is]]))

(deftest sql-vec
  (testing "upsert"
    (with-redefs [smee.db.schema/fetch (fn [] {:idents #{:member/id :member/name :member/email}})]
      (is (= ["insert into member(email, name)\nvalues (?, ?)\n on conflict (email,name) do update set updated_at = now()\nreturning *" "test@test.com" "test"]
             (transact/sql-vec {:member/name "test"
                                :member/email "test@test.com"})))))

  (testing "upsert with one rel"
    (with-redefs [smee.db.schema/fetch (fn [] {:idents #{:member/id :member/name :member/email :token/id}
                                                :joins {:token/member :token/member}})]
      (is (= ["insert into token(ident, member)\nvalues (?, ?)\n on conflict (id) do update set updated_at = now(), ident = excluded.ident, member = excluded.member\nreturning *" "something unique" "test"]
             (transact/sql-vec {:token/ident "something unique"
                                :token/member [:member/name "test"]}))))))
