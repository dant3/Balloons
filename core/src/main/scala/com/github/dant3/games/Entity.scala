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
package com.github.dant3.games

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.{Rectangle, Vector2}

trait Entity extends Stateful with Drawable {
    var x: Float
    var y: Float

    var visible = true

    val velocity:Vector2

    def rect:Rectangle

    override def update(timeDelta: Float) = {
        val move = new Vector2(velocity)
        move.scl(timeDelta)
        x = move.x + x
        y = move.y + y
    }

    override final def draw(batch:SpriteBatch) = {
        if (visible) {
            draw(batch, x, y)
        }
        batch
    }

    protected[this] def draw(batch:SpriteBatch, x:Float, y:Float)
}
