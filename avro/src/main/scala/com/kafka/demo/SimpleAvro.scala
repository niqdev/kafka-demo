package com.kafka.demo

import java.io.File

import com.typesafe.scalalogging.Logger
import org.apache.avro.file.{DataFileReader, DataFileWriter}
import org.apache.avro.io.{DatumReader, DatumWriter}
import org.apache.avro.specific.{SpecificDatumReader, SpecificDatumWriter}

import scala.collection.JavaConverters.asScalaIteratorConverter

/*
 * https://avro.apache.org/docs/current/gettingstartedjava.html
 * https://github.com/sbt/sbt-avro
 */
object SimpleAvro {
  private[this] val logger = Logger(getClass.getSimpleName)

  private[this] def initFile(path: String): File = {
    val file = new File(path)
    file.getParentFile.mkdirs()
    file
  }

  def serializeUsers(users: List[User], path: String): Unit = {
    val userDatumWriter: DatumWriter[User] = new SpecificDatumWriter[User](classOf[User])
    val dataFileWriter: DataFileWriter[User] = new DataFileWriter(userDatumWriter)
    dataFileWriter.create(users.head.getSchema, initFile(path))
    //users.foreach(dataFileWriter.append)
    users.foreach { user =>
      logger.debug(s"serialize user=[$user]")
      dataFileWriter.append(user)
    }
    dataFileWriter.close()
  }

  def deserializeUsers(path: String): List[User] = {
    val userDatumReader: DatumReader[User] = new SpecificDatumReader[User](classOf[User])
    val dataFileReader: DataFileReader[User] = new DataFileReader(initFile(path), userDatumReader)
    val users = dataFileReader.iterator().asScala.toList
    users.foreach { user =>
      logger.debug(s"deserialize user=[$user]")
    }
    users
  }

}
