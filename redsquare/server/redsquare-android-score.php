<?php

// Fill in your information:
define('DATABASE_USER', '');
define('DATABASE_PASSWORD', '');
define('DATABASE_NAME', '');
define('API_KEY', '');

// Create this SQL table:
/*
CREATE TABLE `redsquare_android_scores` (
    `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(255) NOT NULL,
    `score` INT UNSIGNED NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
*/

class Database {
    protected static $pdo, $queryCount;

    public static function connect ($dsn, $user, $password) {
        static::$pdo = new PDO($dsn, $user, $password, [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_OBJ,
            PDO::ATTR_EMULATE_PREPARES => false
        ]);
        static::$queryCount = 0;
    }

    public static function queryCount () {
        return static::$queryCount;
    }

    public static function lastInsertId () {
        return static::$pdo->lastInsertId();
    }

    public static function query ($query, ...$parameters) {
        static::$queryCount++;
        $statement = static::$pdo->prepare($query);
        $statement->execute($parameters);
        return $statement;
    }
}

Database::connect('mysql:host=127.0.0.1;dbname=' . DATABASE_NAME, DATABASE_USER, DATABASE_PASSWORD);

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

$key = isset($_GET['key']) ? $_GET['key'] : '';
if ($key != API_KEY) {
    echo json_encode([ 'success' => false, 'message' => 'The API key is not right' ]);
    exit;
}

$name = isset($_GET['name']) ? $_GET['name'] : null;
$score = isset($_GET['score']) ? $_GET['score'] : null;
if ($name != null && $score != null) {
    Database::query('INSERT INTO `redsquare_android_scores` (`name`, `score`) VALUES (?, ?)', $name, $score);
    echo json_encode([ 'success' => true, 'message' => 'The score is successful stored' ]);
    exit;
}

$page = isset($_GET['page']) ? (int)$_GET['page'] : 1;
if ($page < 1) $page = 1;
$limit = isset($_GET['limit']) ? (int)$_GET['limit'] : 20;
if ($limit < 1) $limit = 1;
if ($limit > 50) $limit = 50;
$scores = Database::query('SELECT `name`, `score` FROM `redsquare_android_scores` ORDER BY `score` DESC LIMIT ?,?', ($page - 1) * $limit, $limit)->fetchAll();
echo json_encode([ 'success' => true, 'scores' => $scores ]);
