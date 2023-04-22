FROM mozilla/sbt

WORKDIR /service-pokemon

COPY . ../../service-pokemon

ENV DB_SERVER_NAME=postgres

CMD sbt "runMain com.kemal.Boot"
