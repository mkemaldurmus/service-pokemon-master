package com.kemal

import akka.http.scaladsl.Http
import com.kemal.config.AppConfig
import com.kemal.config.AppConfig.{serverHost, serverPort}

object Boot extends App with Complements {

  withHandlingErrorF {
    Http()
      .newServerAt(serverHost, serverPort)
      .bind(routes)
      .map(_ => logger.info(s"server start at $serverPort"))
  }
}
