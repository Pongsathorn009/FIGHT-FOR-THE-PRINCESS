package game2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class GAME2 extends JFrame {

    private Image[] backgrounds;
    private int currentBackgroundIndex = 0;
    private Hero hero;
    private List<Monster> monsters = new ArrayList<>();
    private BossMonster bossMonster;
    private Princess princess;
    private HealPotion healPotion;
    private Image fullHeart;
    private Image halfHeart;
    private boolean missionSuccess = false;
    private boolean missionFailed = false;
    private int timeRemaining = 30; // เวลาที่เหลือ 30 วินาที
    private Timer countdownTimer;

    public GAME2() {
        backgrounds = new Image[]{
            new ImageIcon(getClass().getResource("/game2/forest_scene_1.jpg")).getImage(),
            new ImageIcon(getClass().getResource("/game2/forest_scene_2.jpg")).getImage(),
            new ImageIcon(getClass().getResource("/game2/forest_scene_3.jpg")).getImage(),
            new ImageIcon(getClass().getResource("/game2/bgprincess.jpg")).getImage()
        };

        fullHeart = new ImageIcon(getClass().getResource("/game2/full_heart.png")).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        halfHeart = new ImageIcon(getClass().getResource("/game2/half_heart.png")).getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);

        setTitle("FIGHT FOR PRINCESS");
        setSize(900, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        hero = new Hero(this);
        add(new BackgroundPanel());

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                hero.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                hero.keyReleased(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (!hero.isAttacking()) {
                        hero.startAttack();
                    }
                }
            }
        });

        setFocusable(true);

        Timer gameTimer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hero.updatePosition();
                checkCollisions();
                repaint();
            }
        });
        gameTimer.start();

        setupMonsters();
        setupHealPotion();

        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timeRemaining > 0) {
                    timeRemaining--; // ลดเวลาที่เหลือ
                } else {
                    countdownTimer.stop(); // หยุดจับเวลาเมื่อหมดเวลา
                    if (!missionSuccess) {
                        hero.die(); // ถ้าเวลาหมดและยังไม่สำเร็จ ให้ Hero ตาย
                        missionFailed = true;
                    }
                }
                repaint(); // อัพเดทหน้าจอเพื่อแสดงเวลาที่เหลือ
            }
        });
        countdownTimer.start();
    }

    public void nextBackground() {
        if (currentBackgroundIndex < backgrounds.length - 1) {
            currentBackgroundIndex++;
            hero.resetPosition();
            hero.resetJump();
            setupMonsters();
            setupHealPotion();
        }
    }

    private void setupMonsters() {
        monsters.clear();
        bossMonster = null;
        princess = null;

        if (currentBackgroundIndex == backgrounds.length - 1) {
            bossMonster = new BossMonster(
                getClass().getResource("/game2/golem_converted.png"),
                getClass().getResource("/game2/angry_golem_pixel_art.png"),
                getClass().getResource("/game2/spirit.png"),
                getClass().getResource("/game2/stone_golem_attack_converted (1).png"),
                400, 350, 150, 150
            );
            princess = new Princess(getClass().getResource("/game2/princess_pixel_art.png"), 400, 130, 100, 100);
        } else {
            int monsterCount = currentBackgroundIndex + 1;
            int groundY = hero.getY() + hero.getHeight() - 50;

            for (int i = 0; i < monsterCount; i++) {
                int randomX = (int) (Math.random() * (getWidth() - 200));

                monsters.add(new Monster(getClass().getResource("/game2/devil_no_tongue.png"), 
                                         getClass().getResource("/game2/angry_no_tongue.png"), 
                                         randomX, groundY, 50, 50));

                monsters.add(new Monster(getClass().getResource("/game2/devil_with_tongue.png"), 
                                         getClass().getResource("/game2/angry_with_tongue.png"), 
                                         randomX + 60, groundY, 50, 50));
            }
        }
    }

    private void setupHealPotion() {
        healPotion = new HealPotion(getClass().getResource("/game2/health_potion.png"), 
                                    (int) (Math.random() * (getWidth() - 50)), 
                                    450,
                                    30, 30);
    }

    private void checkCollisions() {
        if (hero.isAttacking()) {
            if (bossMonster != null && hero.getBounds().intersects(bossMonster.getBounds())) {
                bossMonster.reduceHp(hero.getAttackPower());
                if (bossMonster.getHp() <= 0) {
                    bossMonster.transformToSpirit();
                    missionSuccess = true;
                    countdownTimer.stop(); // หยุดจับเวลาถ้า mission สำเร็จ
                }
            }

            for (Monster monster : monsters) {
                if (!monster.isDead() && hero.getBounds().intersects(monster.getBounds())) {
                    monster.reduceHp(hero.getAttackPower());
                    break;
                }
            }
        }

        for (Monster monster : monsters) {
            if (!monster.isDead() && hero.getBounds().intersects(monster.getBounds())) {
                monster.startAttackTimer(hero);
            } else {
                monster.resetAttackTimer();
            }
        }

        if (bossMonster != null && hero.getBounds().intersects(bossMonster.getBounds())) {
            bossMonster.becomeAngry();
            bossMonster.attackHero(hero);
        }

        if (healPotion != null && hero.getBounds().intersects(healPotion.getBounds())) {
            hero.increaseHp(1);
            healPotion = null;
        }

        if (hero.isDead()) {
            missionFailed = true;
            countdownTimer.stop(); // หยุดจับเวลาถ้า mission ล้มเหลว
        }
    }

    class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgrounds[currentBackgroundIndex], 0, 0, getWidth(), getHeight(), this);
            hero.draw(g);

            for (Monster monster : monsters) {
                monster.draw(g);
            }

            if (bossMonster != null) {
                bossMonster.draw(g);
            }

            if (princess != null) {
                princess.draw(g);
            }

            if (healPotion != null) {
                healPotion.draw(g);
            }

            drawHeroHp(g);

            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Time Remaining : " + timeRemaining + " s", getWidth() / 2 - 80, 30);

            if (missionSuccess) {
                drawMissionMessage(g, "MISSION SUCCESS!", Color.white);
            } else if (missionFailed) {
                drawMissionMessage(g, "MISSION FAILED", Color.RED);
            }
        }

        private void drawHeroHp(Graphics g) {
            double heroHp = hero.getHp();
            int x = 20;
            int y = 20;

            while (heroHp >= 1) {
                g.drawImage(fullHeart, x, y, this);
                x += 35;
                heroHp -= 1;
            }
            if (heroHp == 0.5) {
                g.drawImage(halfHeart, x, y, this);
            }
        }

        private void drawMissionMessage(Graphics g, String message, Color color) {
            Font font = new Font("Arial", Font.BOLD, 36);
            g.setFont(font);
            FontMetrics metrics = g.getFontMetrics(font);
            int x = (getWidth() - metrics.stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g.setColor(color);
            g.drawString(message, x, y);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GAME2 game = new GAME2();
            game.setVisible(true);
        });
    }
}

class Hero {
    private Image heroImageRight;
    private Image heroImageLeft;
    private Image heroAttackRight;
    private Image heroAttackLeft;
    private Image heroSpirit;
    private Image currentImage;
    private int x, y;
    private int dx = 0;
    private int dy = 0;
    private boolean isJumping = false;
    private boolean isAttacking = false;
    private boolean isDead = false;
    private final int JUMP_STRENGTH = -15;
    private final int GRAVITY = 1;
    private final int GROUND_Y = 400;
    private final int SPEED = 5;
    private final int attackPower = 1;
    private double hp = 3;
    private GAME2 game;
    private int height = 100;

    public Hero(GAME2 game) {
        this.game = game;

        heroImageRight = new ImageIcon(getClass().getResource("/game2/hero_converted.png")).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        heroImageLeft = new ImageIcon(getClass().getResource("/game2/hero_converted2.png")).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        heroAttackRight = new ImageIcon(getClass().getResource("/game2/hero_action_pose_right.png")).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        heroAttackLeft = new ImageIcon(getClass().getResource("/game2/hero_action_pose_left.png")).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        heroSpirit = new ImageIcon(getClass().getResource("/game2/spirit.png")).getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);

        currentImage = heroImageRight;
        resetPosition();
        y = GROUND_Y;
    }

    public void updatePosition() {
        if (isDead) return;

        x += dx;

        if (isJumping) {
            y += dy;
            dy += GRAVITY;

            if (y >= GROUND_Y) {
                y = GROUND_Y;
                isJumping = false;
                dy = 0;
            }
        }

        if (x < 0) {
            x = 0;
        }
        if (x > 750) {
            x = 750;
            game.nextBackground();
        }
    }

    public void draw(Graphics g) {
        g.drawImage(currentImage, x, y, null);
    }

    public int getAttackPower() {
        return attackPower;
    }

    public void keyPressed(KeyEvent e) {
        if (isDead) return;

        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A) {
            dx = -SPEED;
            if (!isAttacking) {
                currentImage = heroImageLeft;
            }
        }
        if (key == KeyEvent.VK_D) {
            dx = SPEED;
            if (!isAttacking) {
                currentImage = heroImageRight;
            }
        }
        if (key == KeyEvent.VK_SPACE && !isJumping) {
            isJumping = true;
            dy = JUMP_STRENGTH;
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_D) {
            dx = 0;
        }
    }

    public void startAttack() {
        if (isDead || isAttacking) return;

        isAttacking = true;
        currentImage = (dx >= 0) ? heroAttackRight : heroAttackLeft;

        Timer attackTimer = new Timer(200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isAttacking = false;
                currentImage = (dx >= 0) ? heroImageRight : heroImageLeft;
            }
        });
        attackTimer.setRepeats(false);
        attackTimer.start();
    }

    public void resetPosition() {
        x = 10;
    }

    public void resetJump() {
        y = GROUND_Y;
        isJumping = false;
        dy = 0;
    }

    public int getY() {
        return y;
    }

    public int getHeight() {
        return height;
    }

    public double getHp() {
        return hp;
    }

    public void increaseHp(double amount) {
        hp += amount;
    }

    public void reduceHp(double amount) {
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            die();
        }
    }

    void die() {
        isDead = true;
        currentImage = heroSpirit;
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean isAttacking() {
        return isAttacking;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 100, 100);
    }
}

class Monster {
    protected Image monsterImage;
    private Image spiritImage;
    private Image angryImage;
    int x;
    int y;
    private int width, height;
    private int hp = 7;
    private boolean isDead = false;
    private Timer attackTimer;

    public Monster(java.net.URL imagePath, java.net.URL angryImagePath, int x, int y, int width, int height) {
        this.monsterImage = new ImageIcon(imagePath).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        this.spiritImage = new ImageIcon(getClass().getResource("/game2/spirit.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        this.angryImage = new ImageIcon(angryImagePath).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics g) {
        if (isDead) {
            g.drawImage(spiritImage, x, y, null);
        } else {
            g.drawImage(monsterImage, x, y, null);
        }
    }

    public int getHp() {
        return hp;
    }

    public void reduceHp(int amount) {
        hp -= amount;
        if (hp <= 0) {
            die();
        }
    }

    private void die() {
        isDead = true;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isDead() {
        return isDead;
    }

    public void startAttackTimer(Hero hero) {
        if (attackTimer == null) {
            monsterImage = angryImage;

            attackTimer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!isDead) hero.reduceHp(0.5);
                }
            });
            attackTimer.setRepeats(true);
            attackTimer.start();
        }
    }

    public void resetAttackTimer() {
        if (attackTimer != null) {
            attackTimer.stop();
            attackTimer = null;
        }
    }
}

class BossMonster extends Monster {
    private int hp = 70;
    private Image angryImage;
    private Image spiritImage;
    private Image attackImage;
    private boolean isAngry = false;
    private boolean isSpirit = false;
    private Timer attackDelayTimer;
    private boolean canAttack = true;

    public BossMonster(java.net.URL imagePath, java.net.URL angryImagePath, java.net.URL spiritImagePath, java.net.URL attackImagePath, int x, int y, int width, int height) {
        super(imagePath, imagePath, x, y, width, height);
        this.angryImage = new ImageIcon(angryImagePath).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        this.spiritImage = new ImageIcon(spiritImagePath).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        this.attackImage = new ImageIcon(attackImagePath).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    public void becomeAngry() {
        if (!isAngry) {
            monsterImage = angryImage;
            isAngry = true;
        }
    }

    public void transformToSpirit() {
        if (!isSpirit) {
            monsterImage = spiritImage;
            isSpirit = true;
        }
    }

    public void attackHero(Hero hero) {
        if (isAngry && canAttack && !isSpirit && hero.getBounds().intersects(getBounds())) {
            monsterImage = attackImage;
            hero.reduceHp(1);
            canAttack = false;

            Timer revertToAngryTimer = new Timer(500, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    monsterImage = angryImage;
                }
            });
            revertToAngryTimer.setRepeats(false);
            revertToAngryTimer.start();

            attackDelayTimer = new Timer(2000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    canAttack = true;
                    attackDelayTimer.stop();
                }
            });
            attackDelayTimer.setRepeats(false);
            attackDelayTimer.start();
        }
    }

    public int getHp() {
        return hp;
    }

    public void reduceHp(int amount) {
        hp -= amount;
        if (hp <= 0) {
            hp = 0;
            transformToSpirit();
        }
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);
        if (!isSpirit) {
            g.setColor(Color.white);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("HP: " + hp, 450, y - 10);
        }
    }
}

class Princess {
    private Image princessImage;
    private int x, y;
    private int width, height;

    public Princess(java.net.URL imagePath, int x, int y, int width, int height) {
        this.princessImage = new ImageIcon(imagePath).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics g) {
        g.drawImage(princessImage, x, y, null);
    }
}

class HealPotion {
    private Image potionImage;
    private int x, y;
    private int width, height;

    public HealPotion(java.net.URL imagePath, int x, int y, int width, int height) {
        this.potionImage = new ImageIcon(imagePath).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics g) {
        g.drawImage(potionImage, x, y, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
