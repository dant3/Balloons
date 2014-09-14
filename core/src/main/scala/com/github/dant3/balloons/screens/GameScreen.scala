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
import com.badlogic.gdx.graphics.{GL20, OrthographicCamera, Pixmap, Texture}
import com.badlogic.gdx.math.Rectangle
import com.github.dant3.balloons.{Balloon, Resources}
import com.github.dant3.utils.Logger

import scala.util.Random

class GameScreen(val game: Game) extends ScreenAdapter with Logger {
    val BALLOON_HIT_SCORE = 1

    var score = 0

    var buttonWasPressed = false
    var balloons:Seq[Balloon] = Seq()
    var missedHitPoints:Seq[(Int, Int)] = Seq()

    var batch:SpriteBatch = new SpriteBatch()
    var camera:OrthographicCamera = new OrthographicCamera()
    camera.setToOrtho(false, 800, 480)
    val uiCamera = new OrthographicCamera()
    uiCamera.setToOrtho(false, Gdx.graphics.getWidth, Gdx.graphics.getHeight)
    uiCamera.update()


    val font = new BitmapFont(Gdx.files.internal(Resources.Fonts.arial))
    val background = new Texture(Gdx.files.internal(Resources.Images.background))
    val missedHit = new Texture(Gdx.files.internal(Resources.Images.hole))
    val cursorOriginalPixmap = new Pixmap(Gdx.files.internal(Resources.Images.target))
    val cursor = new Pixmap(64, 64, Pixmap.Format.RGBA8888)
    cursor.drawPixmap(cursorOriginalPixmap,
                       0, 0, cursorOriginalPixmap.getWidth, cursorOriginalPixmap.getHeight,
                       0, 0, 64, 64)

    val music = Gdx.audio.newMusic(Gdx.files.internal(Resources.Music.backgroundMusic))
    music.play()
    // ---- //
    Gdx.input.setCursorImage(cursor, 32 ,32)

    override def render(delta: Float): Unit = {
        val hit = processInput(delta)
        updateWorld(delta)
        clearScreen()
        drawWorld(batch, hit)
        drawInterface(batch)
    }

    override def dispose(): Unit = {
        for (balloon <- balloons) {
            balloon.dispose()
        }
        background.dispose()
        font.dispose()
        music.dispose()
        missedHit.dispose()
        cursorOriginalPixmap.dispose()
        cursor.dispose()
    }

    override def show(): Unit = resetWorld()

    // ---- //

    private def resetWorld() = {
        score = 0
        balloons.foreach(_.dispose())
        balloons = Seq()
        missedHitPoints = Seq()
    }

    private def processInput(delta: Float):Option[Boolean] = {
        val input = Gdx.input
        val isButtonPressed = input.isButtonPressed(Input.Buttons.LEFT)
        val destroyedSomeBalloons = if (isButtonPressed && !buttonWasPressed) {
            // used pressed button again
            val (x, y) = getInputCoordinates(Input.Buttons.LEFT)

            val destroyedBalloons = for (balloon <- balloons) yield balloon.tryToDestroy(x, y)
            score = score + destroyedBalloons.map(if (_) BALLOON_HIT_SCORE else 0).reduce(_ + _)
            Some(destroyedBalloons.contains(true))
        } else None
        buttonWasPressed = isButtonPressed
        destroyedSomeBalloons
    }

    private def getInputCoordinates(button: Int):(Int,Int) = {
        val input = Gdx.input
        (input.getX(button), Gdx.graphics.getHeight - input.getY(button))
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

    private def drawWorld(batch: SpriteBatch, madeHit: Option[Boolean]) = {
        camera.update()
        batch.setProjectionMatrix(camera.combined)
        batch.begin()
        // 1. - draw background
        batch.draw(background, 0, 0)

        // 2. - draw missed hit
        madeHit match {
            case Some(false) =>
                val inputCoord = getInputCoordinates(Input.Buttons.LEFT)
                val x = inputCoord._1 - missedHit.getWidth / 2
                val y = inputCoord._2 - missedHit.getHeight / 2
                missedHitPoints = missedHitPoints :+ (x, y)
                log.warn(s"Made another missed hit at $inputCoord")
            case _ =>
        }

        // 3. - draw missed hits
        for (missedHitPoint <- missedHitPoints) {
            batch.draw(missedHit, missedHitPoint._1, missedHitPoint._2)
        }

        // 4. - draw balloons on it
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