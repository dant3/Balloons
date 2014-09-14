/**
 * Copyright 2014 Vyacheslav Blinov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.Plugin.androidBuild
import android.Keys._
import sbt.Keys._
import sbt._

import BalloonsBuild._

object Settings {
    lazy val nativeExtractions = SettingKey[Seq[(String, NameFilter, File)]](
        "native-extractions", "(jar name partial, sbt.NameFilter of files to extract, destination directory)"
    )

    lazy val desktopJarName = SettingKey[String]("desktop-jar-name", "name of JAR file for desktop")

    lazy val core = plugins.JvmPlugin.projectSettings ++ Seq(
        version := (version in LocalProject("all-platforms")).value,
        libGDXVersion := (libGDXVersion in LocalProject("all-platforms")).value,
        scalaVersion := (scalaVersion in LocalProject("all-platforms")).value,

        resolvers ++= Repositories.all,

        libraryDependencies ++= Seq(
            Dependencies.libGdxCore(libGDXVersion.value),
            Dependencies.slf$J
        ),

        javacOptions ++= Seq(
            "-Xlint",
            "-encoding", "UTF-8",
            "-source", "1.6",
            "-target", "1.6"
        ),

        scalacOptions ++= Seq(
            "-Xlint",
            "-Ywarn-dead-code",
            "-Ywarn-value-discard",
            "-Ywarn-numeric-widen",
            "-Ywarn-unused",
            "-Ywarn-unused-import",
            "-unchecked",
            "-deprecation",
            "-feature",
            "-encoding", "UTF-8",
            "-target:jvm-1.6"
        ),

        cancelable := true,
        exportJars := true
    )


    lazy val desktop = core ++ Seq(
        libraryDependencies ++= Dependencies.libGdxDesktop(libGDXVersion.value) ++ Seq(
            Dependencies.proguard  % "provided",
            Dependencies.logbackClassic
        ),

        fork in Compile := true,
        unmanagedResourceDirectories in Compile += file("android/assets"),
        desktopJarName := "balloons",
        Tasks.assembly
    )


    lazy val android = core ++ Tasks.natives ++ androidBuild ++ Seq(
        libraryDependencies ++= Dependencies.libGdxAndroid(libGDXVersion.value) ++ Seq(
            Dependencies.logbackAndroid
        ),

        nativeExtractions <<= baseDirectory { base => Seq(
            ("natives-armeabi.jar", new ExactFilter("libgdx.so"), base / "libs" / "armeabi"),
            ("natives-armeabi-v7a.jar", new ExactFilter("libgdx.so"), base / "libs" / "armeabi-v7a"),
            ("natives-x86.jar", new ExactFilter("libgdx.so"), base / "libs" / "x86")
        )},

        platformTarget in Android := "android-19",
        proguardOptions in Android ++= scala.io.Source.fromFile(file("core/proguard-project.txt")).getLines.toList ++
            scala.io.Source.fromFile(file("android/proguard-project.txt")).getLines.toList
    )
}