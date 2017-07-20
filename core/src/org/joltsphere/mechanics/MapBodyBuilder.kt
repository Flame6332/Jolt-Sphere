package org.joltsphere.mechanics

import org.joltsphere.misc.EllipseFixture

import com.badlogic.gdx.maps.Map
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.MapObjects
import com.badlogic.gdx.maps.objects.EllipseMapObject
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.PolylineMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.objects.TextureMapObject
import com.badlogic.gdx.math.Ellipse
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.Shape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array

object MapBodyBuilder {

    // The pixels per tile. If your tiles are 16x16, this is set to 16f
    private var ppt = 0f

    fun buildShapes(map: Map, pixels: Float, world: World, layerName: String): Array<Body> {
        ppt = pixels
        val objects = map.layers.get(layerName).objects

        val bodies = Array<Body>()

        for (`object` in objects) {

            if (`object` is TextureMapObject) {
                continue
            }

            val shape: Shape?
            var isEllipse = false
            var ellipseWidth = 0f
            var ellipseHeight = 0f
            val objectPos = Vector2(0f, 0f)

            if (`object` is RectangleMapObject) {
                shape = getRectangle(`object`)
            } else if (`object` is PolygonMapObject) {
                shape = getPolygon(`object`)
            } else if (`object` is PolylineMapObject) {
                shape = getPolyline(`object`)
            } else if (`object` is EllipseMapObject) {
                val ellipse = `object`.ellipse
                ellipseWidth = ellipse.width / ppt
                ellipseHeight = ellipse.height / ppt
                objectPos.x = ellipse.x / ppt + ellipseWidth / 2f
                objectPos.y = ellipse.y / ppt + ellipseHeight / 2f
                isEllipse = true
                shape = null
            } else {
                continue
            }


            val bd = BodyDef()
            bd.type = BodyType.StaticBody
            bd.position.set(objectPos)
            var body = world.createBody(bd)

            if (isEllipse) {
                body = EllipseFixture.createEllipseFixtures(body, 1f, 0f, 1f, ellipseWidth / 2f, ellipseHeight / 2f, "")
            } else {
                body.createFixture(shape!!, 1f)
                shape.dispose()
            }

            bodies.add(body)

        }
        return bodies
    }

    private fun getRectangle(rectangleObject: RectangleMapObject): PolygonShape {
        val rectangle = rectangleObject.rectangle
        val polygon = PolygonShape()
        val size = Vector2((rectangle.x + rectangle.width * 0.5f) / ppt,
                (rectangle.y + rectangle.height * 0.5f) / ppt)
        polygon.setAsBox(rectangle.width * 0.5f / ppt,
                rectangle.height * 0.5f / ppt,
                size,
                0.0f)
        return polygon
    }

    private fun getPolygon(polygonObject: PolygonMapObject): PolygonShape {
        val polygon = PolygonShape()
        val vertices = polygonObject.polygon.transformedVertices

        val worldVertices = FloatArray(vertices.size)

        for (i in vertices.indices) {
            //System.out.println(vertices[i]);
            worldVertices[i] = vertices[i] / ppt
        }

        polygon.set(worldVertices)
        return polygon
    }

    private fun getPolyline(polylineObject: PolylineMapObject): ChainShape {
        val vertices = polylineObject.polyline.transformedVertices
        val worldVertices = arrayOfNulls<Vector2>(vertices.size / 2)

        for (i in 0..vertices.size / 2 - 1) {
            worldVertices[i] = Vector2()
            worldVertices[i]?.x = vertices[i * 2] / ppt
            worldVertices[i]?.y = vertices[i * 2 + 1] / ppt
        }

        val chain = ChainShape()
        chain.createChain(worldVertices)
        return chain
    }
}