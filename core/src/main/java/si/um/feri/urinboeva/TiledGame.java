package si.um.feri.urinboeva;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class TiledGame extends ApplicationAdapter {

    private OrthographicCamera camera;
    private float tileWidth;
    private float tileHeight;
    private float mapWidthInPx;
    private float mapHeightInPx;
    private Music backgroundMusic;
    private TiledMap tiledMap;
    private TiledMapTileLayer grassLayer;
    private MapLayer mapObjects;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private boolean collisionCooldown = false;
    private Sound sound;
    private Sound sound_coin;
    private Texture chicken;
    private Sprite player;
    private float previousX;
    private float previousY;
    private BitmapFont font;
    private int health;
    private int score;
    private SpriteBatch batch;
    private boolean gameOver = false;
    private OrthographicCamera minimapCamera; // Minimap camera
    private SpriteBatch minimapBatch; // SpriteBatch for the minimap
    private FrameBuffer minimapBuffer;
    @Override
    public void create() {
        // Initialize camera and map
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1600, 1200); // Set viewport size
        tiledMap = new TmxMapLoader().load("assets/untitled.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

   //creating the minimap
        minimapCamera = new OrthographicCamera();
        minimapCamera.setToOrtho(false, camera.viewportWidth, camera.viewportHeight);

        // Initialize the minimap batch
        minimapBatch = new SpriteBatch();

        // Create a FrameBuffer for rendering the minimap
        minimapBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 400, 400, false);

        // Get map layers and tile dimensions
        grassLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Coin");
        tileWidth = grassLayer.getTileWidth();
        tileHeight = grassLayer.getTileHeight();
        mapWidthInPx = grassLayer.getWidth() * tileWidth;
        mapHeightInPx = grassLayer.getHeight() * tileHeight;

        // Set initial camera position
        camera.update();

        // Load assets
        sound = Gdx.audio.newSound(Gdx.files.internal("assets/assets_sounds_Swoosh.mp3"));
        sound_coin = Gdx.audio.newSound(Gdx.files.internal("assets/coin.wav"));
        chicken = new Texture("assets/chicken.png");
        player = new Sprite(chicken);
        player.setPosition(camera.viewportWidth / 2f - player.getWidth() / 2f, 50f);

        font = new BitmapFont();
        batch = new SpriteBatch();
        health = 50;
        score = 0;

        // Load and configure background music
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/background.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.1f);
        backgroundMusic.play();
    }


    @Override
    public void render() {
        if (gameOver) {
            renderGameOver();
            return;
        }
        update();
        ScreenUtils.clear(0f, 0f, 0f, 1f);


        camera.update();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        tiledMapRenderer.getBatch().begin();
        player.draw(tiledMapRenderer.getBatch());
        tiledMapRenderer.getBatch().end();


        renderMinimap();

        // Render health bar
        renderHealthBar();

        // Render score in a fixed position
        batch.begin();
        font.getData().setScale(2);
        font.setColor(1f, 0f, 0f, 1f);
        font.draw(batch, "SCORE: " + score, 10f, Gdx.graphics.getHeight() - 10f);
        batch.end();
    }

    private void renderMinimap() {
        // Render the minimap to the framebuffer
        minimapBuffer.begin();

        ScreenUtils.clear(0, 0, 0, 1);

        // Set the minimap camera's position to follow the player
        float minimapCameraX = player.getX();
        float minimapCameraY = player.getY();

        // Constrain the minimap camera position within the map boundaries
        // Define half the viewport size for boundary constraints
        float halfViewportWidth = minimapCamera.viewportWidth / 2f;
        float halfViewportHeight = minimapCamera.viewportHeight / 2f;

        // Updatin the camera's X position, ensuring it doesn't go beyond the map boundaries
        minimapCameraX = Math.max(halfViewportWidth,
            Math.min(player.getX(), mapWidthInPx - halfViewportWidth));

        // Updating the camera's Y position, ensuring it doesn't go beyond the map boundaries
        minimapCameraY = Math.max(halfViewportHeight,
            Math.min(player.getY(), mapHeightInPx - halfViewportHeight));

       //minimapCameraX = Math.max(1000, Math.min(minimapCameraX, mapWidthInPx - minimapCamera.viewportWidth));
        //minimapCameraY = Math.max(1000, Math.min(minimapCameraY, mapHeightInPx - minimapCamera.viewportHeight));

        minimapCamera.position.set(minimapCameraX, minimapCameraY, 0);
        minimapCamera.update();
        System.out.println("Map Width: " + mapWidthInPx);
        System.out.println("Map Height: " + mapHeightInPx);
        System.out.println("Camera Position: X=" + minimapCameraX + " Y=" + minimapCameraY);
        System.out.println("Viewport Dimensions: W=" + minimapCamera.viewportWidth
            + " H=" + minimapCamera.viewportHeight);
        System.out.println("Player Position: X=" + player.getX() + " Y=" + player.getY());
        System.out.println("Camera Position: X=" + minimapCamera.position.x + " Y=" + minimapCamera.position.y);



        // Adjust the zoom for the minimap (this controls how much of the world is visible)
        minimapCamera.zoom = 1f;

        // Flip the Y-axis for the minimap (fix upside-down)
        minimapCamera.up.set(0, -1, 0); // Flip the Y-axis so the map matches
;

        // Seting the renderer to use the minimap camera's view
        tiledMapRenderer.setView(minimapCamera);
        tiledMapRenderer.render();

        // Draw the player on the minimap (as a small dot or icon)
        minimapBatch.begin();
        TextureRegion chickenRegion = new TextureRegion(chicken);

       // Flip the TextureRegion vertically
        chickenRegion.flip(false, true);

        // Map the player's position from the game world to the minimap (scaled)
        float playerMinimapX = (float) ((2-(player.getX() / mapWidthInPx)) * 1000 - 150f / 2); // Adjust for player size (half the width)
        float playerMinimapY = ((mapHeightInPx - player.getY()) / mapHeightInPx) * 1000 - 150f / 2; // Flip Y-axis for position


        minimapBatch.draw(chickenRegion, playerMinimapX, playerMinimapY, 200f, 200f);
       // System.out.println("Player Minimap Position: X=" + playerMinimapX + " Y=" + playerMinimapY);

        minimapBatch.end();

        minimapBuffer.end();

        // Flip the minimap texture horizontally
        Texture minimapTexture = minimapBuffer.getColorBufferTexture();
        minimapTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegion minimapRegion = new TextureRegion(minimapTexture);
        minimapRegion.flip(false, true); // Flip the texture vertically
        // Draw the flipped minimap on the main screen
        minimapBatch.begin();
        minimapBatch.draw(
            minimapTexture,
            Gdx.graphics.getWidth() - 510 + 500, //  position due to flipping
            Gdx.graphics.getHeight() - 510,
            -500, // Negative width to flip horizontally
            500   // Height of minimap
        );
        minimapBatch.end();
    }




    private void renderHealthBar() {
        float healthBarWidth = 200f;
        float healthWidth = (health / 50f) * healthBarWidth;

        ShapeRenderer shapeRenderer = new ShapeRenderer();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(5f, 10f, healthBarWidth, 20f);
        shapeRenderer.setColor(0f, 1f, 0f, 1f);
        shapeRenderer.rect(5f, 10f, healthWidth, 20f);
        shapeRenderer.end();
    }

    private void update() {
        handlePlayerInput();

        // Ensure the camera stays centered on the player within map bounds
        camera.position.x = Math.max(camera.viewportWidth / 2f, Math.min(player.getX(), mapWidthInPx - camera.viewportWidth / 2f));
        camera.position.y = Math.max(camera.viewportHeight / 2f, Math.min(player.getY(), mapHeightInPx - camera.viewportHeight / 2f));

        // Handle collision and gameplay logic
        handleCollisions();

        camera.update();
    }

    private void handlePlayerInput() {
        previousX = player.getX();
        previousY = player.getY();

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            player.setX(Math.max(0, player.getX() - 250 * Gdx.graphics.getDeltaTime()));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            player.setX(player.getX() + 250 * Gdx.graphics.getDeltaTime());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            player.setY(player.getY() + 250 * Gdx.graphics.getDeltaTime());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            player.setY(Math.max(0, player.getY() - 200 * Gdx.graphics.getDeltaTime()));
        }



        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    private void handleCollisions() {
        int tileX = (int) ((player.getX() + player.getWidth() / 2) / tileWidth);
        int tileY = (int) ((player.getY() + player.getHeight() / 2) / tileHeight);

        if (grassLayer.getCell(tileX, tileY) != null) {
            grassLayer.setCell(tileX, tileY, null);
            score++;
            sound_coin.play();
        }

        mapObjects = tiledMap.getLayers().get("GamObjects");
        if (mapObjects == null) {
            System.out.println("GameObjects layer not found.");
            return;
        }
        MapObjects obstacleObjects = mapObjects.getObjects();
        if (obstacleObjects == null || obstacleObjects.getCount() == 0) {
            System.out.println("No objects found in the Obstacle layer.");
            return;
        }

        // Check collision with obstacles (damage objects)
        boolean isColliding = false;
        for (MapObject mapObject : obstacleObjects) {
            if (mapObject instanceof RectangleMapObject) {
                Rectangle obstacleRect = ((RectangleMapObject) mapObject).getRectangle();

                // Check if the player collides with an obstacle
                if (player.getBoundingRectangle().overlaps(obstacleRect)) {
                    isColliding = true; // Player is colliding with an obstacle

                }
            }
        }

        if (isColliding) {
            // Deduct health only once per frame (if not already in cooldown)
            if (!collisionCooldown) {
                System.out.println("Collision detected with an obstacle!");
                if (health > 0f) { // Ensure health does not go below 0
                    health -= 10f;
                    sound.play();
                    System.out.println(health);
                } else if(health <= 0f) {
                    gameOver = true;

                }
                collisionCooldown = true;
            }
        } else {
            // If the player is no longer touching an obstacle
            collisionCooldown = false;
        }

        TiledMapTileLayer obstacleLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Obstacle");
        if (obstacleLayer != null) {
            TiledMapTileLayer.Cell cell = obstacleLayer.getCell(tileX, tileY);
            if (cell != null) {
                player.setPosition(previousX, previousY);

                //System.out.println("Çarpışma gerçekleşti: " + tileX + ", " + tileY);
            }
            else
            {
                handlePlayerInput();

            }
        }
    }

    private void renderGameOver() {
        batch.begin();
        font.getData().setScale(5, 5);
        font.draw(batch, "Game Over", 800, 1100);
        font.draw(batch, "Rewards Collected: " + score, 700, 1000);
        batch.end();
    }

    @Override
    public void dispose() {
        tiledMap.dispose();
        sound.dispose();
        sound_coin.dispose();
        backgroundMusic.dispose();
        chicken.dispose();
        font.dispose();
    }
}
