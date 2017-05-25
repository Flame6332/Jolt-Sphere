package org.joltshpere.testing.main;

import org.joltshpere.testing.scenes.scene1;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class JoltSphereTesting extends Game {
	
	public static int displayScale = 200;
	public int width = 16 * displayScale;
	public int height = 9 * displayScale;
	
	public static int WIDTH = 16 * displayScale;
	public static int HEIGHT = 9 * displayScale;
	public static final int FPS = 60;
	
	public static String title = "Jolt Sphere Testing";
	
	public BitmapFont font; //testing font
	private Texture fontTex; //texture for font
	
	public OrthographicCamera cam;
	public OrthographicCamera phys2Dcam;
	public Viewport view;
	public Viewport phys2Dview;
	
	public SpriteBatch batch;
	public ShapeRenderer shapeRender;
	
	public static float ppm = 200; //pixels per meter 
	
	@Override
	public void create () {
		
		fontTex = new Texture(Gdx.files.internal("testing/font.png"), true); //mipmaps=true
		fontTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("testing/font.fnt"), new TextureRegion(fontTex), false); //flipped=font
		
		cam = new OrthographicCamera();
		phys2Dcam = new OrthographicCamera();
		
		cam.setToOrtho(false, width, height);
		phys2Dcam.setToOrtho(false, width / ppm, height / ppm); //physics world by meters
		view = new ExtendViewport(width, height, cam);
		phys2Dview = new ExtendViewport(width / ppm, height / ppm, phys2Dcam);
		
		this.setScreen(new scene1(this));
		
		Gdx.graphics.setVSync(true);
		
		batch = new SpriteBatch();
		shapeRender = new ShapeRenderer();
		
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		super.render();
		
		Gdx.graphics.setTitle(title + " : " + subtitle + "     FPS: " + Gdx.graphics.getFramesPerSecond());

		if (Gdx.input.isKeyJustPressed(Keys.ENTER) && !Gdx.graphics.isFullscreen()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
			else if (Gdx.input.isKeyJustPressed(Keys.ENTER)) Gdx.graphics.setWindowedMode(width, height);
		
		batch.setProjectionMatrix(cam.combined);
		shapeRender.setProjectionMatrix(cam.combined);
	}
		
	String subtitle = "Basic";
	
		int currentScene = 1;
		
	public void switchScene() {
		currentScene++;
		switch (currentScene) {
			case 1:	this.setScreen(new scene1(this)); subtitle = "Basic";
				break;
			default: this.setScreen(new scene1(this)); currentScene = 1; subtitle = "Basic";
				break;
		}
	}
	
	@Override 
	public void resize (int width, int height) {
		view.update(width, height);
		phys2Dview.update(width, height);
	}
	
	public void dispose () {
		 
	}
}
