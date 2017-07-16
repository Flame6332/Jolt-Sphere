package org.joltsphere.main;

import org.joltsphere.misc.RandomFunctions;
import org.joltsphere.scenes.Scene1;
import org.joltsphere.scenes.Scene2;
import org.joltsphere.scenes.Scene3;
import org.joltsphere.scenes.Scene4;
import org.joltsphere.scenes.Scene5;

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

public class JoltSphereMain extends Game {
	
	public static int displayScale = 200;
	public int width = 16 * displayScale;
	public int height = 9 * displayScale;
	
	public static int WIDTH = 16 * displayScale;
	public static int HEIGHT = 9 * displayScale;
	public static final int FPS = 60;
	
	public static String title = "Jolt Sphere";
	
	public BitmapFont font; //testing font
	private Texture fontTex; //texture for font
	
	public OrthographicCamera cam;
	public OrthographicCamera UIcam;
	public OrthographicCamera phys2Dcam;
	public Viewport view;
	public Viewport phys2Dview;
	public Viewport UIview;
	
	public SpriteBatch batch;
	public ShapeRenderer shapeRender;
	
	public static float ppm = 200; //pixels per meter 
	
	@Override
	public void create () {
		
		fontTex = new Texture(Gdx.files.internal("testing/font.png"), true); //mipmaps=true
		fontTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("testing/font.fnt"), new TextureRegion(fontTex), false); //flipped=font
		
		cam = new OrthographicCamera();
		UIcam = new OrthographicCamera();
		phys2Dcam = new OrthographicCamera();
		
		cam.setToOrtho(false, width, height);
		UIcam.setToOrtho(false, width, height);
		phys2Dcam.setToOrtho(false, width / ppm, height / ppm); //physics world by meters
		view = new ExtendViewport(width, height, cam);
		UIview = new ExtendViewport(width, height, UIcam);
		phys2Dview = new ExtendViewport(width / ppm, height / ppm, phys2Dcam);
		
		this.setScreen(new Scene1(this));
		
		Gdx.graphics.setVSync(true);
		setFPSLimit(60); 
		Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
		
		batch = new SpriteBatch();
		shapeRender = new ShapeRenderer();
		
		System.out.println("My Kotlin Test: add(1,2) returns: " + RandomFunctions.add(1,2));
		
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (Gdx.input.isKeyJustPressed(Keys.TAB)) switchScene();
		
		super.render(); //renders in screens
		
		cam.position.set(width/2, height/2, 0);
		cam.zoom = 1;
		phys2Dcam.position.set(width/2/ppm, height/2/ppm, 0);
		phys2Dcam.zoom = 1;
		
		Gdx.graphics.setTitle(title + " : " + subtitle + "     FPS: " + Gdx.graphics.getFramesPerSecond());

		if (Gdx.input.isKeyJustPressed(Keys.ENTER) && !Gdx.graphics.isFullscreen()) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
			else if (Gdx.input.isKeyJustPressed(Keys.ENTER)) Gdx.graphics.setWindowedMode(width, height);
		
		batch.setProjectionMatrix(UIcam.combined); 
		shapeRender.setProjectionMatrix(cam.combined);
	}
		
	String subtitle = "Basic";
	
	int currentScene = 1;
		
	public void switchScene() {
		currentScene++;
		switch (currentScene) {
			case 1:	this.setScreen(new Scene1(this)); subtitle = "Arena";
				break;
			case 2: this.setScreen(new Scene2(this)); subtitle = "StreamBeam";
				break;
			case 3: this.setScreen(new Scene3(this)); subtitle = "Mountain Climber";
				break;
			case 4: this.setScreen(new Scene4(this)); subtitle = "Tower Defense";
				break;
			case 5: this.setScreen(new Scene5(this)); subtitle = "Ninja Chase";
				break;
			default: this.setScreen(new Scene1(this)); currentScene = 1; subtitle = "Arena";
				break;
		}
	}
	
	@Override 
	public void resize (int width, int height) {
		view.update(width, height);
		UIview.update(width, height);
		phys2Dview.update(width, height);
	}
	
	public void dispose () {
		 
	}
	
	public class input implements UniversalControllerInput {
	
		
		
	}
	
	protected void setFPSLimit(int value) {} // overidden in desktop launcher
	
}
