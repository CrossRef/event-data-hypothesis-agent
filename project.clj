(defproject event-data-hypothesis-agent "0.2.2"
  :description "Crossref Event Data hypothesis.com Agent"
  :url "http://eventdata.crossref.org"
  :license {:name "The MIT License (MIT)"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.crossref.event-data-agent-framework "0.2.0"]
                 [event-data-common "0.1.30"]
                 [throttler "1.0.0"]
                 [commons-codec/commons-codec "1.10"]
                 [robert/bruce "0.8.0"]
                 [clj-http "2.3.0"]]
  :main ^:skip-aot event-data-hypothesis-agent.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :prod {:resource-paths ["config/prod"]}
             :dev  {:resource-paths ["config/dev"]}})
