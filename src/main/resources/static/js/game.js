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
var enemies;

var game;


var ENEMY_SPEED = 1/250000;

var BULLET_DAMAGE = 50;

function preload() {
    this.load.atlas('sprites', 'assets/spritesheet.png', 'assets/spritesheet.json');
    this.load.image('bullet', 'assets/bullet.png');
}

var Enemy = new Phaser.Class({

        Extends: Phaser.GameObjects.Image,
        pathNum: 0,
        initialize:

        function Enemy (scene)
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

            console.log("starting on path "+this.pathNum);

            paths[this.pathNum].getPoint(this.follower.t, this.follower.vec);
            
            this.setPosition(this.follower.vec.x, this.follower.vec.y);            
        },
        receiveDamage: function(damage) {
            this.hp -= damage;           
            
            // if hp drops below 0 we deactivate this enemy
            if(this.hp <= 0) {
                this.setActive(false);
                this.setVisible(false);      
            }
        },
        update: function (time, delta)
        {
            var path = paths[this.pathNum];
            this.follower.t += ENEMY_SPEED * delta * path.speed;
            path.getPoint(this.follower.t, this.follower.vec);

            this.setPosition(this.follower.vec.x, this.follower.vec.y);

            if (this.follower.t >= 1)
            {
                this.setActive(false);
                this.setVisible(false);
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

    this.nextEnemy = 0;

    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(gotLocation);
    } else {
        $("#debug").text("Geolocation is not supported by this browser.");
    }
}

function gotLocation(location) {
    var graphics = game.add.graphics();

    count = 0;

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
                } else {
                    paths[count].lineTo(node.x, node.y);
                }

            });

            //graphics.lineStyle(2, getRandomInt(256)*getRandomInt(256)*getRandomInt, 1);
            graphics.lineStyle(2, 0xffffff, 1);
            paths[count].draw(graphics);
            enemies = game.physics.add.group({ classType: Enemy, runChildUpdate: true });

            turrets = game.add.group({ classType: Turret, runChildUpdate: true });

            bullets = game.physics.add.group({ classType: Bullet, runChildUpdate: true });

            game.physics.add.overlap(enemies, bullets, damageEnemy);

            game.input.on('pointerdown', placeTurret);

            count++;
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

function update(time, delta) {

   // console.log("time="+time+", nextEnemy="+this.nextEnemy+" paths len="+paths.length);
    if ((paths.length>0) && (time > this.nextEnemy))
    {
        var enemy = enemies.get();
        console.log("Making new enemy - "+enemy);
        if (enemy)
        {
            enemy.setActive(true);
            enemy.setVisible(true);
            enemy.startOnPath();

            this.nextEnemy = time + 2000;
        }       
    }
}

function canPlaceTurret(i, j) {
    return true;
}

function placeTurret(pointer) {
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

function addBullet(x, y, angle) {
    var bullet = bullets.get();
    if (bullet)
    {
        bullet.fire(x, y, angle);
    }
}