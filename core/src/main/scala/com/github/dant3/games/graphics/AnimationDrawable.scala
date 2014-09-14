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
package com.github.dant3.games.graphics

import com.badlogic.gdx.graphics.g2d.{Animation, SpriteBatch}
import com.github.dant3.games.{Disposable, Stateful}

class AnimationDrawable(val animation: Animation) extends Stateful with Disposable {
    private var runTime = 0f
    private var running = false
    var visible = true

    def start() = reset(); running = true
    def reset() = runTime = 0
    def pause() = running = false
    def stop()  = pause(); reset()

    def isRunning = running
    def isFinished = !running

    override def update(timeDelta: Float) = {
        if (running) {
            runTime = runTime + timeDelta
        }
        running = !animation.isAnimationFinished(runTime)
    }

    def draw(batch: SpriteBatch, x:Float, y: Float): SpriteBatch = {
        if (visible) {
            batch.draw(animation.getKeyFrame(runTime), x, y)
        }
        batch
    }

    def draw(batch: SpriteBatch, x:Float, y: Float, w:Float, h:Float): SpriteBatch = {
        if (visible) {
            batch.draw(animation.getKeyFrame(runTime), x, y, w, h)
        }
        batch
    }

    override def dispose() = {
        animation.getKeyFrames.foreach(_.getTexture.dispose())
    }
}


object AnimationDrawable {
    def apply(animation: Animation) = new AnimationDrawable(animation)
    def unapply(animationDrawable: AnimationDrawable):Animation = animationDrawable.animation
}