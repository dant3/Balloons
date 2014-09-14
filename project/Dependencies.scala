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
import sbt._

object Dependencies {
    def libGdxCore(version: String) = "com.badlogicgames.gdx" % "gdx" % version

    def libGdxDesktop(version: String) = Seq(
        "com.badlogicgames.gdx" % "gdx-backend-lwjgl" % version,
        "com.badlogicgames.gdx" % "gdx-platform" % version classifier "natives-desktop"
    )

    def libGdxAndroid(version: String) = Seq(
        "com.badlogicgames.gdx" % "gdx-backend-android" % version,
        "com.badlogicgames.gdx" % "gdx-platform" % version % "natives" classifier "natives-armeabi",
        "com.badlogicgames.gdx" % "gdx-platform" % version % "natives" classifier "natives-armeabi-v7a",
        "com.badlogicgames.gdx" % "gdx-platform" % version % "natives" classifier "natives-x86"
    )

    lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.1.2"
    lazy val logbackAndroid = "com.github.tony19" % "logback-android-classic" % "1.1.1-3"
    lazy val proguard = "net.sf.proguard" % "proguard-base" % "4.11"
    lazy val slf$J = "org.slf4j" % "slf4j-api" % "1.7.7"
}