package com.github.javifdev.survey

import com.typesafe.config.ConfigFactory
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

/**
 * @author Ignacio Navarro Martín
 */
trait ConfigurationTwitter {
  val configuration = ConfigFactory.load()
  val configurationBuilder = new ConfigurationBuilder()
    .setDebugEnabled(true)
    .setOAuthConsumerKey(configuration.getString("consumer.key"))
    .setOAuthConsumerSecret(configuration.getString("consumer.secret"))
    .setOAuthAccessToken(configuration.getString("access.key"))
    .setOAuthAccessTokenSecret(configuration.getString("access.secret"))
}
