(ns smee.db.query-test
  (:require [smee.db.query :as q]
            [smee.db.schema]
            [clojure.test :refer [deftest testing is]]))

(deftest where-test
  (testing "where with and, not"
    (is (= (q/where '[and [member/name != ""]
                          [member/id 1]])
           {:where "where (member.name != ? and member.id = ?)" :args '("" 1)})))

  (testing "where not and implicit and"
    (is (= (q/where '[[member/name != ""]
                      [member/id ""]])
           {:where "where (member.name != ? and member.id = ?)" :args '("" "")})))

  (testing "where or"
    (is (= (q/where '[or [member/name ""]
                         [member/id 1]
                         [member/active != false]])
           {:where "where (member.name = ? or member.id = ? or member.active != ?)" :args '("" 1 false)})))

  (testing "where and & or"
    (is (= (q/where '[or [member/name "test"]
                         [member/id 1]
                      and [member/id 2]
                          [member/active != false]])
           {:where "where (member.name = ? or member.id = ?) and (member.id = ? and member.active != ?)"
            :args '("test" 1 2 false)}))))

(deftest sql-vec
  (testing "sql-vec with select, limit and where clause"
    (is (= ["select member.name as member$name, member.email as member$email\nfrom member\nwhere (member.name = ? and member.email = ?)\nlimit 1" "test" "test@test.com"]
           (q/sql-vec '[:select member/name member/email
                        :where [member/name "test"]
                               [member/email "test@test.com"]
                        :limit 1]))))

  (testing "sql-vec with an or where clause"
    (is (= (q/sql-vec '[:select member/name member/email
                        :where or [member/name "test"]
                                  [member/email "test@test.com"]
                               and [member/name != nil]
                                   [member/id != 1]
                        :limit 1])
           ["select member.name as member$name, member.email as member$email\nfrom member\nwhere (member.name = ? or member.email = ?) and (member.name is not null and member.id != ?)\nlimit 1" "test" "test@test.com" 1])))

  (testing "a sql-vec that tries out most of the stuff"
    (is (= ["select member.id as member$id, member.name as member$name, member.email as member$email, todo.name as todo$name, todo.id as todo$id\nfrom member\nwhere (member.id != ? and todo.name != ?)\norder by todo.name desc, member.name asc\noffset 10\nlimit 1" 1 "hello"]
           (q/sql-vec '[:select member/id member/name member/email todo/name todo/id
                        :where and [member/id != 1]
                                   [todo/name != "hello"]
                        :limit 1
                        :offset 10
                        :order todo/name desc member/name]))))

  (testing "a join with a select statement that doesn't include the main table"
    (with-redefs [smee.db.schema/fetch (fn [] {:member/todos {:db/joins :todo/member :db/type :many}})]
      (is (= ["select todo.name as todo$name\nfrom member\njoin todo on todo.member = member.id\nwhere (todo.name is not null)"]
             (q/sql-vec '[:select todo/name
                          :joins member/todos
                          :where [todo/name != nil]])))))

  (testing "variable parameters"
    (let [ids [1 2 3]]
      (is (= ["select todo.name as todo$name\nfrom todo\nwhere (todo.id in (?, ?, ?))" 1 2 3]
             (q/sql-vec '[:select todo/name
                          :where [todo/id ?ids]]
                        {:ids ids}))))))

(deftest from
  (testing "from with a select without the main table"
    (is (= "from member"
           (q/from '[todo/name] '[todo/member])))))
