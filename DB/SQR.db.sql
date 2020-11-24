BEGIN TRANSACTION;
DROP TABLE IF EXISTS `MasterData`;
CREATE TABLE IF NOT EXISTS `MasterData` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`Ord_id`	TEXT UNIQUE,
	`Ord`	TEXT,
	`Cust`	TEXT,
	`Nomen`	TEXT,
	`Attrib`	TEXT,
	`Q_ord`	INTEGER,
	`Q_box`	INTEGER,
	`N_box`	INTEGER,
	`DT`	DateTime NOT NULL DEFAULT (strftime('%d-%m-%Y %H:%M:%S', 'now', 'localtime')
);
DROP TABLE IF EXISTS `Deps`;
CREATE TABLE IF NOT EXISTS `Deps` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`Id_deps`	TEXT,
	`Name_Deps`	TEXT
);
DROP TABLE IF EXISTS `Boxes`;
CREATE TABLE IF NOT EXISTS `Boxes` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`Q_box`	INTEGER,
	`N_box`	INTEGER,
	`DT`	DateTime NOT NULL DEFAULT (strftime('%d-%m-%Y %H:%M:%S', 'now', 'localtime'),
	`Id_m`	INTEGER,
	`Oper`	INTEGER,
	FOREIGN KEY(`Id_m`) REFERENCES `MasterData`(`_id`),
	FOREIGN KEY(`Id_d`) REFERENCES `Deps`(`_id`)
);
DROP INDEX IF EXISTS `idx_boxes`;
CREATE UNIQUE INDEX IF NOT EXISTS `idx_boxes` ON `Boxes` (
	`Id_m`,
	`Oper`,
	`N_box`
);
DROP TABLE IF EXISTS `Prods`;
CREATE TABLE IF NOT EXISTS `Prods` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`Id_b`	INTEGER,
	`Id_d`	INTEGER,
	`RQ_box`	INTEGER,
	`P_date`	Date NOT NULL DEFAULT (strftime('%d-%m-%y', 'now', 'localtime')),
	FOREIGN KEY(`Id_b`) REFERENCES `Boxes`(`_id`),
	FOREIGN KEY(`Id_d`) REFERENCES `Deps`(`_id`)
);
DROP INDEX IF EXISTS `idx_prods`;
CREATE UNIQUE INDEX IF NOT EXISTS `idx_prods` ON `Prods` (
	`Id_b`,
	`Id_d`,
	`P_date`
);
COMMIT;