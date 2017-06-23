package org.joltsphere.mechanics;

import org.joltsphere.main.JoltSphereMain;
import org.joltsphere.misc.EllipseFixture;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class WorldEntities {
	
	float ppm = JoltSphereMain.ppm;
	
	public World world;
	
	public void createPlatform1(World world) {
		 
		this.world = world;
		
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.StaticBody;
		bdef.position.x = 0;
		bdef.position.y = 0;
		 
		ChainShape chain = new ChainShape();
		Vector2[] v = new Vector2[12];
		int x = JoltSphereMain.WIDTH / 2;
		
		int xpnt1 = 800, ypnt1 = 300;
		int xpnt2 = 760, ypnt2 = 250;
		int xpnt3 = 690, ypnt3 = 190;
		int xpnt4 = 600, ypnt4 = 140;
		int xpnt5 = 490, ypnt5 = 115;
		int xpnt6 = 410, ypnt6 = 100;
		
		v[0] = new Vector2((x-xpnt1) / ppm, ypnt1 / ppm);
		v[1] = new Vector2((x-xpnt2)  / ppm, ypnt2 / ppm);
		v[2] = new Vector2((x-xpnt3)  / ppm, ypnt3 / ppm);
		v[3] = new Vector2((x-xpnt4)  / ppm, ypnt4 / ppm);
		v[4] = new Vector2((x-xpnt5)  / ppm, ypnt5 / ppm);
		v[5] = new Vector2((x-xpnt6) / ppm, ypnt6 / ppm);
		
		v[11] = new Vector2((x+xpnt1) / ppm, ypnt1 / ppm);
		v[10] = new Vector2((x+xpnt2) / ppm, ypnt2 / ppm);
		v[9] = new Vector2((x+xpnt3) / ppm, ypnt3 / ppm);
		v[8] = new Vector2((x+xpnt4) / ppm, ypnt4 / ppm);
		v[7] = new Vector2((x+xpnt5) / ppm, ypnt5 / ppm);
		v[6] = new Vector2((x+xpnt6) / ppm, ypnt6 / ppm);
			
		chain.createChain(v);
		FixtureDef fdef  = new FixtureDef();
		fdef.shape = chain;
		fdef.friction = 1;
		fdef.restitution = 0;
		
		world.createBody(bdef).createFixture(fdef).setUserData("ground");
		
		chain.dispose();
		
	}

	public void createFlatPlatform(World world) {
		 
		this.world = world;
		
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.StaticBody;
		bdef.position.x = 0;
		bdef.position.y = 0;
		 
		int w = JoltSphereMain.WIDTH, h = JoltSphereMain.HEIGHT;
		ChainShape chain = new ChainShape();
		Vector2[] v = new Vector2[5];
		
		v[0] = new Vector2((0 +1) / ppm, (0 +1) / ppm);
		v[1] = new Vector2((w -1) / ppm, (0 +1) / ppm);
		v[2] = new Vector2((w -1) / ppm, (h -1) / ppm);
		v[3] = new Vector2((0 +1) / ppm, (h -1) / ppm);
		v[4] = new Vector2((0 +1) / ppm, (1 +1)/ ppm);
			
		chain.createChain(v);
		FixtureDef fdef  = new FixtureDef();
		fdef.shape = chain;
		fdef.friction = 1;
		fdef.restitution = 0;
		
		world.createBody(bdef).createFixture(fdef).setUserData("ground");
		
		chain.dispose();
		
	}
	
	public void createPlatform2(World world) {
		 
		this.world = world;
		
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.StaticBody;
		bdef.position.x = 0;
		bdef.position.y = 0;
		 
		ChainShape chain = new ChainShape();
		Vector2[] v = new Vector2[12];
		int x = JoltSphereMain.WIDTH / 2;
		
		int xpnt1 = 800, ypnt1 = 170;
		int xpnt2 = 700, ypnt2 = 100;
		int xpnt3 = 300, ypnt3 = 100;
		int xpnt4 = 200, ypnt4 = 150;
		int xpnt5 = 150, ypnt5 = 200;
		int xpnt6 = 1, ypnt6 = 400;
		
		v[0] = new Vector2((x-xpnt1) / ppm, ypnt1 / ppm);
		v[1] = new Vector2((x-xpnt2)  / ppm, ypnt2 / ppm);
		v[2] = new Vector2((x-xpnt3)  / ppm, ypnt3 / ppm);
		v[3] = new Vector2((x-xpnt4)  / ppm, ypnt4 / ppm);
		v[4] = new Vector2((x-xpnt5)  / ppm, ypnt5 / ppm);
		v[5] = new Vector2((x-xpnt6) / ppm, ypnt6 / ppm);
		
		v[11] = new Vector2((x+xpnt1) / ppm, ypnt1 / ppm);
		v[10] = new Vector2((x+xpnt2) / ppm, ypnt2 / ppm);
		v[9] = new Vector2((x+xpnt3) / ppm, ypnt3 / ppm);
		v[8] = new Vector2((x+xpnt4) / ppm, ypnt4 / ppm);
		v[7] = new Vector2((x+xpnt5) / ppm, ypnt5 / ppm);
		v[6] = new Vector2((x+xpnt6) / ppm, ypnt6 / ppm);
			
		chain.createChain(v);
		FixtureDef fdef  = new FixtureDef();
		fdef.shape = chain;
		fdef.friction = 1;
		
		world.createBody(bdef).createFixture(fdef).setUserData("ground");
		
		chain.dispose();
		
	}
	
	public void createPlatform3(World world) {
		 
		this.world = world;
		
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.StaticBody;
		bdef.position.x = 0;
		bdef.position.y = 0;
		 
		ChainShape chain = new ChainShape();
		Vector2[] v = new Vector2[12];
		int x = JoltSphereMain.WIDTH / 2;
		
		int xpnt1 = 800, ypnt1 = -20;
		int xpnt2 = 600, ypnt2 = 100;
		int xpnt3 = 500, ypnt3 = 120;
		int xpnt4 = 400, ypnt4 = 150;
		int xpnt5 = 300, ypnt5 = 180;
		int xpnt6 = 150, ypnt6 = 200;
		
		v[0] = new Vector2((x-xpnt1) / ppm, ypnt1 / ppm);
		v[1] = new Vector2((x-xpnt2)  / ppm, ypnt2 / ppm);
		v[2] = new Vector2((x-xpnt3)  / ppm, ypnt3 / ppm);
		v[3] = new Vector2((x-xpnt4)  / ppm, ypnt4 / ppm);
		v[4] = new Vector2((x-xpnt5)  / ppm, ypnt5 / ppm);
		v[5] = new Vector2((x-xpnt6) / ppm, ypnt6 / ppm);
		
		v[11] = new Vector2((x+xpnt1) / ppm, ypnt1 / ppm);
		v[10] = new Vector2((x+xpnt2) / ppm, ypnt2 / ppm);
		v[9] = new Vector2((x+xpnt3) / ppm, ypnt3 / ppm);
		v[8] = new Vector2((x+xpnt4) / ppm, ypnt4 / ppm);
		v[7] = new Vector2((x+xpnt5) / ppm, ypnt5 / ppm);
		v[6] = new Vector2((x+xpnt6) / ppm, ypnt6 / ppm);
			
		chain.createChain(v);
		FixtureDef fdef  = new FixtureDef();
		fdef.shape = chain;
		fdef.friction = 1;
		
		world.createBody(bdef).createFixture(fdef).setUserData("ground");
		
		chain.dispose();
		
	}
	
	public void createPlatform4(World world) {
		 
		this.world = world;
		
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.StaticBody;
		bdef.position.x = JoltSphereMain.WIDTH /2 /ppm;
		bdef.position.y = JoltSphereMain.HEIGHT * 0.0f / ppm;
		
		CircleShape circle = new CircleShape();
		circle.setRadius(800/ppm);			
		
		FixtureDef fdef  = new FixtureDef();
		fdef.shape = circle;
		fdef.friction = 1;
		
		world.createBody(bdef).createFixture(fdef).setUserData("ground");
		
		bdef.position.y = 1200 / ppm;
		bdef.type = BodyType.DynamicBody;
		EllipseFixture.createEllipseFixtures(world.createBody(bdef), fdef, 135/ppm, 60 /ppm, "");
		bdef.position.y = 1700 / ppm;
		EllipseFixture.createEllipseFixtures(world.createBody(bdef), fdef, 135/ppm, 60 /ppm, "");
		bdef.position.y = 800 / ppm;
		EllipseFixture.createEllipseFixtures(world.createBody(bdef), fdef, 135/ppm, 60 /ppm, "");
		
		circle.dispose();
		
		
		
	}
	
}
