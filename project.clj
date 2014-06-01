(defproject com.scintillance "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.namespace "0.2.4"]]
  :profiles {
              :dev {
                     :source-paths ["dev"]
                     :dependencies [[org.clojure/tools.namespace "0.2.4"]]}
              :leiningen/reply {:dependencies [[org.slf4j/jcl-over-slf4j "1.7.2"]]
                                :exclusions [commons-logging]}}
  :local-repo-classpath true
  :jvm-opts ["-Dhttp.nonProxyHosts=10.153.*"])
