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
package com.github.dant3.balloons

object Resources {
    object Images {
        lazy val background = img("background.jpg")
        lazy val balloons = for (index <- 1 to 7) yield img(s"balloon$index.png")
        lazy val hole = img("hole.png")
        lazy val piercer = img("piercer.png")
        lazy val target = img("target.png")
        lazy val sheet = img("sheet.png")

        private[this] def img(fileName: String) = "img/" + fileName
    }
    
    object Animations {
        lazy val explosion = img("explosion.png")
        lazy val bang = img("bang.gif")
        private[this] def img(fileName: String) = "img/" + fileName
    }

    object Sounds {
        lazy val pop = snd(s"pop.mp3")

        private[this] def snd(fileName: String) = "snd/" + fileName
    }

    object Music {
        lazy val backgroundMusic = "snd/music.mp3"
    }

    object Fonts {
        lazy val arial = "fnt/arial.fnt"
    }
}
