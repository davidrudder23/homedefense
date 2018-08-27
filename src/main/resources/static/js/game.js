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

var paths = [];
var turrets;
var home;
var enemies;

var game;

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

            this.pathNum = getRandomInt(paths.length-1);
            this.direction=1;

            paths[this.pathNum].getPoint(this.follower.t, this.follower.vec);
            
            this.setPosition(this.follower.vec.x, this.follower.vec.y);            
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

    game.add = this.add;
    game.physics = this.physics;
    game.input = this.input;
    game.physics = this.physics;

    enemies = this.physics.add.group({ classType: Enemy, runChildUpdate: true });

    turrets = this.add.group({ classType: Turret, runChildUpdate: true });

    home = this.add.group({ classType: Home, runChildUpdate: true });
    this.physics.add.overlap(enemies, home, damageHome);

    bullets = this.physics.add.group({ classType: Bullet, runChildUpdate: true });

    this.physics.add.overlap(enemies, bullets, damageEnemy);

    this.nextEnemy = 0;

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(gotLocation, handleNoGeo);
    } else {
        handleNoGeo();
    }
}

function handleNoGeo() {
    $("#nogeo").show();
}

/*
This function takes the response from the "no geo" select - which
popular destination we want to defend - and passes it to the real
function */
function geoToDestination() {
    var suppliedGeo = $("#newGeo").val();
    var values = suppliedGeo.split("x");
    var location={};
    location.coords={};
    location.coords.latitude=parseFloat(values[0]);
    location.coords.longitude=parseFloat(values[1]);
    gotLocation(location);
}

function gotLocation(location) {
    if (!location) {
        handleNoGeo();
        return;
    }

    var graphics = game.add.graphics();

    count = 0;

    // create the geo bounding box. the aspect ratio appears to be off
    // but, it appears to work ¯\_(ツ)_/¯
    var north = (location.coords.latitude + 0.0075).toFixed(4);
    var west = (location.coords.longitude - 0.0150).toFixed(4);
    var south = (location.coords.latitude - 0.0075).toFixed(4);
    var east = (location.coords.longitude + 0.0150).toFixed(4);
    console.log("north="+north);
    console.log("west="+west);
    console.log("south="+south);
    console.log("east="+east);


    $.get("/maps/640/480/"
        +north+"/"
        +west+"/"
        +south+"/"
        +east,
        function(data) {
        $.each(data.ways, function(index, way) {
            $.each(way.nodes, function(index, node) {
                if (paths.length <= count) {
                    paths[count] = game.add.path(node.x, node.y);
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

        scoreText = game.add.text(16, 16, 'Score: 0', { fontSize: '18px', fill: '#2255ff', backgroundColor: '#fff' });

        game.input.on('pointerdown', placeTurret);
        game.input.on('pointerdown', function(pointer) {
            console.log("pointer down");
        });
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
                enemy.setInteractive(true);
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