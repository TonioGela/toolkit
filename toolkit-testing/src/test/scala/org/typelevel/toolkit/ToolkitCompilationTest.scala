/*
 * Copyright 2023 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.toolkit

import munit.{CatsEffectSuite, TestOptions}
import cats.effect.IO
import fs2.Stream
import fs2.io.file.Files
import buildinfo.BuildInfo.{version, scalaVersion}

class ToolkitCompilationTest extends CatsEffectSuite {

  testCompilation213("Toolkit should compile a simple Hello Cats Effect") {
    s"""|import cats.effect._
        |
        |object Hello extends IOApp.Simple {
        |  def run = IO.println("Hello toolkit!")
        |}"""
  }

  testCompilation3("Toolkit should compile a simple Hello Cats Effect") {
    s"""|import cats.effect.*
        |
        |object Hello extends IOApp.Simple:
        |  def run = IO.println("Hello toolkit!")"""
  }

  testCompilation213("Toolkit should compile a script with every dependency") {
    s"""|import cats.syntax.all._
        |import cats.effect._
        |import com.monovore.decline.effect._
        |import fs2.data.csv.generic.semiauto._
        |import fs2.io.file._
        |import io.circe._
        |import org.http4s.ember.client._
        |
        |object Hello extends IOApp.Simple {
        |  def run = IO.println("Hello toolkit!")
        |}"""
  }

  testCompilation3("Toolkit should compile a script with every dependency") {
    s"""|import cats.syntax.all.*
        |import cats.effect.*
        |import com.monovore.decline.effect.*
        |import fs2.data.csv.generic.semiauto.*
        |import fs2.io.file.*
        |import org.http4s.ember.client.*
        |
        |object Hello extends IOApp.Simple:
        |  def run = IO.println("Hello toolkit!")"""
  }

  def testCompilation213: String => String => Unit = testCompilation("2.13.12")

  def testCompilation3: String => String => Unit = testCompilation("3.3.1")

  def testCompilation(
      expectedLangVersion: String
  )(testName: String)(scriptBody: String): Unit = {
    val options: TestOptions = TestOptions(s"$testName - $expectedLangVersion")
    val testOptions: TestOptions =
      if (scalaVersion == expectedLangVersion) options else options.ignore
    test(testOptions)(
      Files[IO]
        .tempFile(None, "", ".scala", None)
        .use { path =>
          val header =
            s"//> using scala $scalaVersion\n//> using toolkit typelevel:$version\n"
          Stream(header, scriptBody.stripMargin)
            .through(Files[IO].writeUtf8(path))
            .compile
            .drain >> ScalaCliProcess.compile(path.toString)
        }
    )
  }

}
