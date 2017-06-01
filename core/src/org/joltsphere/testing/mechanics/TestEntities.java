package org.joltsphere.testing.mechanics;

import org.joltsphere.testing.main.JoltSphereTesting;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class TestEntities {
	
	float ppm = JoltSphereTesting.ppm;
	
	public World world;
	
	public void createPlatform(World realWorld) {
		 
		world = realWorld;
		
		BodyDef bdef = new BodyDef();
		bdef.type = BodyType.StaticBody;
		bdef.position.x = 0;
		bdef.position.y = 0;
		 
		ChainShape chain = new ChainShape();
		Vector2[] v = new Vector2[12];
		int x = JoltSphereTesting.WIDTH / 2;
		
		int xpnt1 = 800;
		int xpnt2 = 760;
		int xpnt3 = 690;
		int xpnt4 = 600;
		int xpnt5 = 490;
		int xpnt6 = 410;
		
		int ypnt1 = 300;
		int ypnt2 = 250;
		int ypnt3 = 190;
		int ypnt4 = 140;
		int ypnt5 = 115;
		int ypnt6 = 100;
		
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

}
