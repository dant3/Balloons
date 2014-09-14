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
package com.github.dant3.balloons.screens

import com.badlogic.gdx._
import com.badlogic.gdx.graphics.g2d.{BitmapFont, SpriteBatch}
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera, Texture}
import com.badlogic.gdx.math.Rectangle
import com.github.dant3.balloons.{Balloon, Resources}

import scala.util.Random

class GameScreen(val game: Game) extends ScreenAdapter {
    val BALLOON_HIT_SCORE = 1

    var score = 0

    var buttonWasPressed = false
    var balloons:Seq[Balloon] = Seq()

    var batch:SpriteBatch = new SpriteBatch()
    var camera:OrthographicCamera = new OrthographicCamera()
    camera.setToOrtho(false, 800, 480)
    val uiCamera = new OrthographicCamera()
    uiCamera.setToOrtho(false, Gdx.graphics.getWidth, Gdx.graphics.getHeight)
    uiCamera.update()


    val font = new BitmapFont(Gdx.files.internal(Resources.Fonts.arial))
    val background = new Texture(Gdx.files.internal(Resources.Images.background))

    val music = Gdx.audio.newMusic(Gdx.files.internal(Resources.Music.backgroundMusic))
    music.play()
    // ---- //

    override def render(delta: Float): Unit = {
        processInput(delta)
        updateWorld(delta)
        clearScreen()
        drawWorld(batch)
        drawInterface(batch)
    }

    override def dispose(): Unit = {
        for (balloon <- balloons) {
            balloon.dispose()
        }
        background.dispose()
        font.dispose()
        music.dispose()
    }

    override def show(): Unit = resetWorld()

    // ---- //

    private def resetWorld() = {
        score = 0
        balloons = Seq()
    }

    private def processInput(delta: Float) = {
        val input = Gdx.input
        val isButtonPressed = input.isButtonPressed(Input.Buttons.LEFT)
        if (isButtonPressed && !buttonWasPressed) {
            // used pressed button again
            val (x, y) = (input.getX(Input.Buttons.LEFT), input.getY(Input.Buttons.LEFT))

            val destroyedBalloons = for (balloon <- balloons) yield balloon.tryToDestroy(x, y)
            score = score + destroyedBalloons.map(if (_) BALLOON_HIT_SCORE else 0).reduce(_ + _)
        }
        buttonWasPressed = isButtonPressed
    }

    private def updateWorld(timeDelta: Float) = {
        val groupedBalloons = balloons groupBy isAliveAndVisible
        groupedBalloons.get(false).map(_.foreach(_.dispose()))
        balloons = groupedBalloons.getOrElse(true, Seq())

        val worldWidth = Gdx.graphics.getWidth


        // --- create some new balloons --- //
        if (balloons.size < 2) {
            balloons = balloons :+ new Balloon(worldWidth - Random.nextInt(worldWidth), 0)
        }
        // --- //

        for (balloon <- balloons) {
            balloon.update(timeDelta)
        }
    }

    private def isAliveAndVisible(balloon: Balloon) = balloon.isAlive match {
        case false => false
        case true => balloon.rect.overlaps(worldRect)
    }

    private def worldRect = new Rectangle(0, 0, Gdx.graphics.getWidth, Gdx.graphics.getHeight)

    private def drawWorld(batch: SpriteBatch) = {
        camera.update()
        batch.setProjectionMatrix(camera.combined)
        batch.begin()
        // 1. - draw background
        batch.draw(background, 0, 0)


        // 2. - draw balloons on it
        for (balloon <- balloons) {
            balloon.draw(batch)
        }

        batch.end()
    }

    def drawInterface(batch: SpriteBatch): Unit = {
        batch.setProjectionMatrix(uiCamera.combined)
        batch.begin()

        font.draw(batch, "Score: " + score, 0, Gdx.graphics.getHeight)

        batch.end()
    }

    private def clearScreen() = {
        Gdx.gl.glClearColor(1, 0, 0, 1)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }
}
