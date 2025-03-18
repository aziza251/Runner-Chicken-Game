package si.um.feri.urinboeva;

import static com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable.draw;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
    private Texture santaClaus;
    private Sprite player;
    float previousX;
    float previousY;
    private BitmapFont font;

    private int health;
    private int score;
    private Batch batch;

    private boolean gameOver = false;  // Flag to control game-over state

    @Override
    public void create() {
        // Initialize camera and map
        camera = new OrthographicCamera();
        tiledMap = new TmxMapLoader().load("assets/MyMap.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        // Get map layers and set up camera
        grassLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Coin");

        tileWidth = grassLayer.getTileWidth();
        tileHeight = grassLayer.getTileHeight();
        mapWidthInPx = grassLayer.getWidth() * tileWidth;
        mapHeightInPx = grassLayer.getHeight() * tileHeight;

        camera.setToOrtho(false, mapWidthInPx, mapHeightInPx);
        camera.update();

        // Load sound and textures
        sound = Gdx.audio.newSound(Gdx.files.internal("assets/assets_sounds_Swoosh.mp3"));
        sound_coin = Gdx.audio.newSound(Gdx.files.internal("assets/coin.wav"));
        santaClaus = new Texture("assets/Outline.png");
        player = new Sprite(santaClaus);
        player.setPosition(camera.viewportWidth / 2f - player.getWidth() / 2f, 0);

        font = new BitmapFont();
        batch = new SpriteBatch();
        health = 50;
        score = 0;

        // Load background music
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/background.mp3"));
        backgroundMusic.setLooping(true); // Loop the music
        backgroundMusic.setVolume(0.1f); // Adjust volume (0.0 to 1.0)
        backgroundMusic.play(); // Start playing
    }


    @Override
    public void render() {
        if (gameOver) {
            renderGameOver();
            return;
        }
        update();

        // Clear the screen
        ScreenUtils.clear(0f, 0f, 0f, 1f);

        // Handle input for camera and gameplay
        handleConfigurationInput();

        // Update camera and render map
        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        // Render player and game info
        tiledMapRenderer.getBatch().begin();
        player.draw(tiledMapRenderer.getBatch());
        font.getData().setScale(2);
        font.setColor(1f, 0f, 0f, 1f);
        font.draw(tiledMapRenderer.getBatch(), "SCORE: " + score, 10f, 90f);
        tiledMapRenderer.getBatch().end();

        // Health bar rendering
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

    private void handleConfigurationInput() {
        // Define screen width and height
        float screenWidth = 1600f;
        float screenHeight = 1600f;

        // Save previous positions
        previousX = player.getX();
        previousY = player.getY();

        // Handle player movement with arrow keys
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            // Move left and ensure player stays within the left edge of the screen
            player.setX(Math.max(0, player.getX() - 200 * Gdx.graphics.getDeltaTime()));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            // Move right and ensure player stays within the right edge of the screen
            player.setX(Math.min(screenWidth - player.getWidth(), player.getX() + 200 * Gdx.graphics.getDeltaTime()));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            // Move up and ensure player stays within the top edge of the screen
            player.setY(Math.min(screenHeight - player.getHeight(), player.getY() + 200 * Gdx.graphics.getDeltaTime()));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            // Move down and ensure player stays within the bottom edge of the screen
            player.setY(Math.max(0, player.getY() - 200 * Gdx.graphics.getDeltaTime()));
        }

        // Reset camera position
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            camera.position.set(screenWidth / 2f, screenHeight / 2f, 0);
        }


    // Toggle layer visibility
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            tiledMap.getLayers().get("Background").setVisible(!tiledMap.getLayers().get("Background").isVisible());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            tiledMap.getLayers().get("Coin").setVisible(!tiledMap.getLayers().get("Coin").isVisible());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            tiledMap.getLayers().get("Obstacle").setVisible(!tiledMap.getLayers().get("Obstacle").isVisible());
        }

        // Exit game on ESC
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }


    private void update() {
        // Calculate tile coordinates for collision detection
        int tileX = (int) ((player.getX() + player.getWidth() / 2) / tileWidth);
        int tileY = (int) ((player.getY() + player.getHeight() / 2) / tileHeight);

        // Collect grass (or coins) by removing the tile and increasing score
        if (grassLayer.getCell(tileX, tileY) != null) {
            grassLayer.setCell(tileX, tileY, null); // Remove the tile
            score++; // Increase score for collecting grass
            sound_coin.play();
        }

        mapObjects = tiledMap.getLayers().get("GameObjects");
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
                if (health > 0) { // Ensure health does not go below 0
                    health -= 10;
                    sound.play();
                } else if(health == 0) {
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

            }
            else
            {
                handleConfigurationInput();

            }
        }

    }

    private void renderGameOver() {

        batch.begin();
        font.getData().setScale(5, 5);
        font.draw(this.batch, "Game Over", 770.0F, Gdx.graphics.getHeight() - 600.0F);
        font.draw(this.batch, "Rewards Collected: " + this.score, 670.0F, Gdx.graphics.getHeight() - 700.0F);
        batch.end();
    }


    @Override
    public void dispose() {
        // Dispose resources
        tiledMap.dispose();
        sound.dispose();
        sound_coin.dispose();
        backgroundMusic.dispose();
        santaClaus.dispose();
        font.dispose();
    }
}
