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

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.{Animation, SpriteBatch}
import com.badlogic.gdx.math.Vector2
import com.github.dant3.games.graphics.{AnimationDrawable, GifDecoder}
import com.github.dant3.games.{Entity, TextureEntity}
import com.github.dant3.utils.Logger

import scala.util.Random

class Balloon(override var x:Float, override var y: Float) extends Entity with TextureEntity with Logger {
    override val velocity: Vector2 = new Vector2(0, 100 + Random.nextInt(200))

    private[this] var state = State.Normal

    private[this] val blowAnimation = AnimationDrawable(GifDecoder.loadGIFAnimation(
        Animation.PlayMode.NORMAL,
        500,
        Gdx.files.internal(Resources.Animations.bang).read()
    ))

    protected lazy val texture = new Texture(Gdx.files.internal(
        Resources.Images.balloons(Random.nextInt(Resources.Images.balloons.size))
    ))

    private[this] val popSound = Gdx.audio.newSound(Gdx.files.internal(Resources.Sounds.pop))


    override def dispose() = {
        super.dispose()
        blowAnimation.dispose()
        popSound.dispose()
    }


    def tryToDestroy(x: Float, y: Float): Boolean = state match {
        case State.Blowing => false
        case State.Normal =>
            val balloonRectangle = squareRect
            val destroyed = balloonRectangle.contains(x, y)
            if (destroyed) {
                state = State.Blowing
                blowAnimation.start()
                popSound.play(1.0f)
            }
            destroyed
    }

    def squareRect = {
        val myRect = rect
        myRect.setY(myRect.y + myRect.height - myRect.width)
        myRect.setHeight(myRect.width)
    }

    def isAlive = !isDestroyed
    def isDestroyed: Boolean = state == State.Blowing && blowAnimation.isFinished

    override def update(timeDelta: Float): Unit = state match {
        case State.Normal => super.update(timeDelta)
        case _ =>    blowAnimation.update(timeDelta)
    }

    override def draw(batch: SpriteBatch, x:Float, y:Float) = state match {
        case State.Normal => super.draw(batch, x, y)
        case _ =>    blowAnimation.draw(batch, x, y + texture.getHeight - texture.getWidth, texture.getWidth, texture.getWidth)
    }
}


private object State extends Enumeration {
    type State = Value
    val Normal, Blowing = State.Value
}
