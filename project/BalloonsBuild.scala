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

object BalloonsBuild extends Build with GDXKeys {
    lazy val core = Project(
        id       = "core",
        base     = file("core"),
        settings = Settings.core
    )

    lazy val desktop = Project(
        id       = "desktop",
        base     = file("desktop"),
        settings = Settings.desktop
    ).dependsOn(core)

    lazy val android = Project(
        id       = "android",
        base     = file("android"),
        settings = Settings.android
    ).dependsOn(core)

    lazy val all = Project(
        id       = "all-platforms",
        base     = file("."),
        settings = Settings.core
    ).aggregate(core, desktop, android)
}

