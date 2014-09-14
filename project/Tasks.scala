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
import sbt.Keys._
import sbt._

object Tasks {
    import Settings.nativeExtractions

    lazy val extractNatives = TaskKey[Unit]("extract-natives", "Extracts native files")

    lazy val natives = Seq(
        ivyConfigurations += config("natives"),
        nativeExtractions := Seq.empty,
        extractNatives <<= (nativeExtractions, update) map { (ne, up) =>
            val jars = up.select(configurationFilter("natives"))
            ne foreach { case (jarName, fileFilter, outputPath) =>
                jars find(_.getName.contains(jarName)) map { jar =>
                    IO.unzip(jar, outputPath, fileFilter)
                }
            }
        },
        compile in Compile <<= (compile in Compile) dependsOn extractNatives
    )

    import java.io.{File => JFile}
    import Settings.desktopJarName

    lazy val assemblyKey = TaskKey[Unit]("assembly", "Assembly desktop using Proguard")

    lazy val assembly = assemblyKey <<= (fullClasspath in Runtime, // dependency to make sure compile finished
        target, desktopJarName, version, // data for output jar name
        javaOptions in Compile, managedClasspath in Compile, // java options and classpath
        classDirectory in Compile, dependencyClasspath in Compile, update in Compile, // classes and jars to proguard
        streams) map { (c, target, name, ver, options, cp, cd, dependencies, up, s) =>
        val provided = Set(up.select(configurationFilter("provided")):_*)
        val compile = Set(up.select(configurationFilter("compile")):_*)
        val runtime = Set(up.select(configurationFilter("runtime")):_*)
        val optional = Set(up.select(configurationFilter("optional")):_*)
        val onlyProvidedNames = provided -- compile -- runtime -- optional
        val (onlyProvided, withoutProvided) = dependencies.partition(cpe => onlyProvidedNames contains cpe.data)
        val exclusions = Seq("!META-INF/MANIFEST.MF", "!library.properties").mkString(",")
        val inJars = withoutProvided.map("\""+_.data.absolutePath+"\"("+exclusions+")").mkString(JFile.pathSeparator)
        val libraryJars = onlyProvided.map("\""+_.data.absolutePath+"\"").mkString(JFile.pathSeparator)
        val outfile = "\""+(target/"%s-%s.jar".format(name, ver)).absolutePath+"\""
        val classfiles = "\"" + cd.absolutePath + "\""
        val manifest = "\"" + file("desktop/manifest").absolutePath + "\""
        val proguardOptions = scala.io.Source.fromFile(file("core/proguard-project.txt")).getLines.toList ++
            scala.io.Source.fromFile(file("desktop/proguard-project.txt")).getLines.toList
        val proguard = options ++ Seq("-cp", Path.makeString(cp.files), "proguard.ProGuard") ++ proguardOptions ++ Seq(
            "-injars", classfiles,
            "-injars", inJars,
            "-injars", manifest,
            "-libraryjars", libraryJars,
            "-outjars", outfile)
        s.log.info("preparing proguarded assembly")
        s.log.debug("Proguard command:")
        s.log.debug("java "+proguard.mkString(" "))
        val exitCode = Process("java", proguard) ! s.log
        if (exitCode != 0) {
            sys.error("Proguard failed with exit code [%s]" format exitCode)
        } else {
            s.log.info("Output file: "+outfile)
        }
    }
}