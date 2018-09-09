var config = {
    type: Phaser.AUTO,
    parent: 'content',
    width: 640,
    height: 512,
    physics: {
        default: 'arcade'
    },
    scene: {
        key: 'main',
        preload: preload,
        create: create,
        update: update
    }
};

var game = new Phaser.Game(config);

var currentNorth = 0;
var currentSouth = 0;
var currentEast = 0;
var currentWest = 0;

var paths = [];
var turrets;
var home;
var enemies;

var globals = {};

var scoreText;

var score = 0;

var ENEMY_SPEED = 1/250000;

var MAX_ENEMIES = 8;

var BULLET_DAMAGE = 50;

function preload() {
    this.load.atlas('sprites', 'assets/handwriting_spritesheet.png', 'assets/handwriting_spritesheet.json');
    this.load.image('bullet', 'assets/bullet.png');
}

var Enemy = new Phaser.Class({

        Extends: Phaser.GameObjects.Image,
        pathNum: 0,
        direction: 1,
        initialize: function Enemy (scene)
        {
            Phaser.GameObjects.Image.call(this, scene, 0, 0, 'sprites', 'enemy');

            this.follower = { t: 0, vec: new Phaser.Math.Vector2() };
            this.hp = 0;
        },

        startOnPath: function ()
        {
            if (paths.length<=0) {
                return;
            }
            this.follower.t = 0;
            this.hp = 100;

            var availablePathNums = [];
            for (var i = 0; i < paths.length; i++) {
                if (paths[i].startPoint.x<0 || paths[i].startPoint.y<0) {
                    availablePathNums[availablePathNums.length] = i;
                }
            }

            this.pathNum = availablePathNums[getRandomInt(availablePathNums.length-1)];
            this.direction=1;
            paths[this.pathNum].getPoint(this.follower.t, this.follower.vec);

            this.setPosition(this.follower.vec.x, this.follower.vec.y);
            $("#messages").text("An enemy is coming on "+paths[this.pathNum].name+"!!");
        },
        receiveDamage: function(damage) {
            this.hp -= damage;           
            
            // if hp drops below 0 we deactivate this enemy
            if(this.hp <= 0) {
                this.setActive(false);
                this.setVisible(false);

                addScore(10);
            }
        },
        update: function (time, delta)
        {
            var path = paths[this.pathNum];
            this.follower.t += this.direction * ENEMY_SPEED * delta * path.speed;
            path.getPoint(this.follower.t, this.follower.vec);

            this.setPosition(this.follower.vec.x, this.follower.vec.y);

            if (this.follower.t >= 1)
            {
                var foundNewPath = false;
                for (var i = 0 ; i < paths.length; i++) {
                    //console.log("Comparing existing="+paths[i].curves[0].p0.x+"x"+paths[i].curves[0].p0.y+" vs path["+i+"]="+paths[i].curves[0].p0.x+"x"+paths[i].curves[0].p0.y);
                    if ((paths[i].curves[0].p0.x == Math.round(this.follower.vec.x)) &&
                        (paths[i].curves[0].p0.y == Math.round(this.follower.vec.y))) {
                        //console.log(this.pathNum+" found new path["+i+"]="+paths[i].curves[0].p0.x+"x"+paths[i].curves[0].p0.y+" with speed "+paths[i].speed);
                        this.pathNum = i;
                        foundNewPath = true;
                        this.follower.t = 0;
                        continue;
                    }
                }

                if (!foundNewPath) {
                    //console.log (this.pathNum+" enemy ended at "+Math.round(this.follower.vec.x)+"x"+Math.round(this.follower.vec.y));

                    this.direction *= -1;
                }
            }
        },

});

function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
}

function getEnemy(x, y, distance) {
    var enemyUnits = enemies.getChildren();
    for(var i = 0; i < enemyUnits.length; i++) {       
        if(enemyUnits[i].active && Phaser.Math.Distance.Between(x, y, enemyUnits[i].x, enemyUnits[i].y) < distance)
            return enemyUnits[i];
    }
    return false;
} 

var Turret = new Phaser.Class({

        Extends: Phaser.GameObjects.Image,

        initialize:

        function Turret (scene)
        {
            Phaser.GameObjects.Image.call(this, scene, 0, 0, 'sprites', 'turret');
            this.nextTic = 0;
        },
        place: function(i, j) {            
            this.y = i * 64 + 64/2;
            this.x = j * 64 + 64/2;
        },
        fire: function() {
            var enemy = getEnemy(this.x, this.y, 200);
            if(enemy) {
                var angle = Phaser.Math.Angle.Between(this.x, this.y, enemy.x, enemy.y);
                addBullet(this.x, this.y, angle);
                this.angle = (angle + Math.PI/2) * Phaser.Math.RAD_TO_DEG;
            }
        },
        update: function (time, delta)
        {
            if(time > this.nextTic) {
                this.fire();
                this.nextTic = time + 1000;
            }
        }
});

var Home = new Phaser.Class({

        Extends: Phaser.GameObjects.Image,

        initialize:

        function Turret (scene)
        {
            Phaser.GameObjects.Image.call(this, scene, 0, 0, 'sprites', 'home');
            this.nextTic = 0;
        },
        place: function(i, j) {
            this.x = i;
            this.y = j;
        },
        fire: function() {
            var enemy = getEnemy(this.x, this.y, 200);
            if(enemy) {
                var angle = Phaser.Math.Angle.Between(this.x, this.y, enemy.x, enemy.y);
                addBullet(this.x, this.y, angle);
            }
        },
        update: function (time, delta)
        {
            if(time > this.nextTic) {
                this.fire();
                this.nextTic = time + 1000;
            }
        }
});
    
var Bullet = new Phaser.Class({

        Extends: Phaser.GameObjects.Image,

        initialize:

        function Bullet (scene)
        {
            Phaser.GameObjects.Image.call(this, scene, 0, 0, 'bullet');

            this.incX = 0;
            this.incY = 0;
            this.lifespan = 0;

            this.speed = Phaser.Math.GetSpeed(600, 1);
        },

        fire: function (x, y, angle)
        {
            this.setActive(true);
            this.setVisible(true);
            //  Bullets fire from the middle of the screen to the given x/y
            this.setPosition(x, y);
            
        //  we don't need to rotate the bullets as they are round
        //    this.setRotation(angle);

            this.dx = Math.cos(angle);
            this.dy = Math.sin(angle);

            this.lifespan = 1000;
        },

        update: function (time, delta)
        {
            this.lifespan -= delta;

            this.x += this.dx * (this.speed * delta);
            this.y += this.dy * (this.speed * delta);

            if (this.lifespan <= 0)
            {
                this.setActive(false);
                this.setVisible(false);
            }
        }

    });

 
function create() {
    // bomb if no session, but this should have already been taken care of
    var sessionToken = localStorage.getItem("sessionToken");
    if (!sessionToken) {
        window.location="login.html";
        return;
    }

    globals.add = this.add;
    globals.physics = this.physics;
    globals.input = this.input;
    globals.physics = this.physics;

    enemies = this.physics.add.group({ classType: Enemy, runChildUpdate: true });

    turrets = this.add.group({ classType: Turret, runChildUpdate: true });

    home = this.add.group({ classType: Home, runChildUpdate: true });
    this.physics.add.overlap(enemies, home, damageHome);

    bullets = this.physics.add.group({ classType: Bullet, runChildUpdate: true });

    this.physics.add.overlap(enemies, bullets, damageEnemy);

    this.nextEnemy = 0;

    var graphics = globals.add.graphics();

    count = 0;

    $.ajax({
        type: "GET",
        url: "/maps/640/480",
        headers: {
            "X-Authorization-Token": sessionToken
        },
        success: function(data) {
            $.each(data.ways, function(index, way) {
                $.each(way.nodes, function(index, node) {
                    if (paths.length <= count) {
                        paths[count] = globals.add.path(node.x, node.y);
                        paths[count].speed = way.maxSpeed;
                        paths[count].name = way.name;
                    } else {
                        paths[count].lineTo(node.x, node.y);
                    }

                });

                //graphics.lineStyle(2, getRandomInt(256)*getRandomInt(256)*getRandomInt(256), 1);
                graphics.lineStyle(2, 0xffffff, 1);
                paths[count].draw(graphics);

                count++;
            });
            placeHome();

            scoreText = globals.add.text(16, 16, 'Score: 0', { fontSize: '18px', fill: '#2255ff', backgroundColor: '#fff' });

            globals.input.on('pointerdown', placeTurret);
         }
    });

}

function damageEnemy(enemy, bullet) {  
    // only if both enemy and bullet are alive
    if (enemy.active === true && bullet.active === true) {
        // we remove the bullet right away
        bullet.setActive(false);
        bullet.setVisible(false);    
        
        // decrease the enemy hp with BULLET_DAMAGE
        enemy.receiveDamage(BULLET_DAMAGE);
    }
}

function damageHome(enemy, home) {
    // only if both enemy and bullet are alive
    if (enemy.active === true) {
        // Finish game
        console.log("Home was hit!");
    }
}

function update(time, delta) {

    // console.log("time="+time+", nextEnemy="+this.nextEnemy+" paths len="+paths.length);
    if ((paths.length>0) && (time > this.nextEnemy))
    {
        var numActiveEnemies = enemies.children.entries.filter(e=>e.active).length;
        if (numActiveEnemies<MAX_ENEMIES) {
            var enemy = enemies.get();
            //console.log("Making new enemy - "+enemy);
            if (enemy)
            {
                //enemy.setInteractive(true);
                enemy.on("pointerdown", function(pointer) { console.log("enemy pointer down")}, this);
                enemy.setActive(true);
                enemy.setVisible(true);
                enemy.startOnPath();

                this.nextEnemy = time + 1000;
            }
        }
    }
}

function canPlaceTurret(i, j) {
    return true;
}

function placeTurret(pointer) {
    console.log("Place turret");
    var i = Math.floor(pointer.y/64);
    var j = Math.floor(pointer.x/64);
    if(canPlaceTurret(i, j)) {
        var turret = turrets.get();
        if (turret)
        {
            turret.setActive(true);
            turret.setVisible(true);
            turret.place(i, j);
        }   
    }
}

function placeHome(pointer) {
    var house = home.get();
    if (house)
    {
        house.setActive(true);
        house.setVisible(true);
        house.place(320, 240);
    }
}

function addBullet(x, y, angle) {
    var bullet = bullets.get();
    if (bullet)
    {
        bullet.fire(x, y, angle);
    }
}

function addScore(amount) {
    score += amount;
    scoreText.setText("Score: "+score);
}