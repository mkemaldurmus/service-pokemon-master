FROM mozilla/sbt

WORKDIR /service-pokemon

COPY . /service-pokemon
CMD sbt "runMain com.kemal.Importer"
