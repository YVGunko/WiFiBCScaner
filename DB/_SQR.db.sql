BEGIN TRANSACTION;
DROP INDEX IF EXISTS `idx_boxes`;
DROP INDEX IF EXISTS `idx_prods`;
DROP TABLE IF EXISTS `Prods`;
DROP TABLE IF EXISTS `Boxes`;
DROP TABLE IF EXISTS `MasterData`;
DROP TABLE IF EXISTS `Defs`;
DROP TABLE IF EXISTS `Deps`;
DROP TABLE IF EXISTS `Opers`;
CREATE TABLE IF NOT EXISTS `Opers` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`Opers`	TEXT
);
CREATE TABLE IF NOT EXISTS `Deps` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`Id_deps`	TEXT,
	`Name_Deps`	TEXT
);
CREATE TABLE IF NOT EXISTS `Defs` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`Host_IP`	TEXT,
	`Port`	TEXT,
	`Id_d`	INTEGER,
	`Id_o`	INTEGER,	
	FOREIGN KEY(`Id_d`) REFERENCES `Deps`(`_id`),
	FOREIGN KEY(`Id_o`) REFERENCES `Opers`(`_id`)
);
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
	`DT`	DateTime NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS `Boxes` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`Id_m`	INTEGER,
	`Id_o`	INTEGER,
	`Q_box`	INTEGER,
	`N_box`	INTEGER,
	`DT`	DateTime NOT NULL DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY(`Id_m`) REFERENCES `MasterData`(`_id`),
	FOREIGN KEY(`Id_o`) REFERENCES `Opers`(`_id`)
);
CREATE UNIQUE INDEX IF NOT EXISTS `idx_boxes` ON `Boxes` (
	`Id_m`,
	`Id_o`,
	`N_box`
);
CREATE TABLE IF NOT EXISTS `Prods` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`Id_b`	INTEGER,
	`Id_d`	INTEGER,
	`RQ_box`	INTEGER,
	`P_date`	Date NOT NULL,
	FOREIGN KEY(`Id_b`) REFERENCES `Boxes`(`_id`),
	FOREIGN KEY(`Id_d`) REFERENCES `Deps`(`_id`)
);

CREATE UNIQUE INDEX IF NOT EXISTS `idx_prods` ON `Prods` (
	`Id_b`,
	`Id_d`,
	`P_date`
);
COMMIT;
BEGIN TRANSACTION;

INSERT INTO `Deps` (Id_deps,Name_Deps) VALUES ('000000020','Бригада ТЭП №1 Каблуки');
INSERT INTO `Deps` (Id_deps,Name_Deps) VALUES ('000000027','Бригада ТЭП №1 Ранты');
INSERT INTO `Deps` (Id_deps,Name_Deps) VALUES ('000000001',"Бригада ТЭП №1 Стойки");
INSERT INTO `Deps` (Id_deps,Name_Deps) VALUES ("000000013","Бригада ТЭП №1 Тройки");
INSERT INTO `Deps` (Id_deps,Name_Deps) VALUES ('000000014','Бригада ТЭП №1 Цвет');
INSERT INTO `Deps` (Id_deps,Name_Deps) VALUES ('000000019','Бригада ТЭП №2 Каблуки');
INSERT INTO `Deps` (Id_deps,Name_Deps) VALUES ('000000028','Бригада ТЭП №2 Ранты');
INSERT INTO `Deps` (Id_deps,Name_Deps) VALUES ('000000004','Бригада ТЭП №2 Стойки');
INSERT INTO `Deps` (Id_deps,Name_Deps) VALUES ('000000015','Бригада ТЭП №2 Тройки');
INSERT INTO `Deps` (Id_deps,Name_Deps) VALUES ('000000016','Бригада ТЭП №2 Цвет');

INSERT INTO `Opers` VALUES (1,'Производство');
INSERT INTO `Opers` VALUES (2,'Отгрузка');
INSERT INTO `Opers` VALUES (3,'Покраска');

INSERT INTO `Defs` VALUES (1,'192.168.1.44','4242',1,1);

INSERT INTO `MasterData` (Ord_id,Ord,Cust,Nomen,Attrib,Q_ord,Q_box,N_box) VALUES ('712000100100001034.11.0.50','S7-120-0010','Саркисян','Терминатор Черн./№11 Рант.Беж №40','Вст.№16 Шпал.598',50,20,3);
INSERT INTO `MasterData` (Ord_id,Ord,Cust,Nomen,Attrib,Q_ord,Q_box,N_box) VALUES ('712000100100001034.115.0.50','S7-120-0010','Саркисян','Терминатор Черн./№11 Рант.Беж №40','Вст.№16/15',50,20,3);
INSERT INTO `MasterData` (Ord_id,Ord,Cust,Nomen,Attrib,Q_ord,Q_box,N_box) VALUES ('712000100100001034.115.1.50','S7-120-0010','Саркисян','Терминатор Черн./№11 Рант.Беж №42','Вст.№16/15',5,5,1);
INSERT INTO `MasterData` (Ord_id,Ord,Cust,Nomen,Attrib,Q_ord,Q_box,N_box) VALUES ('712000100100001034.114.0.150','S7-120-0010','Саркисян','Терминатор Черн./№11 Рант.Беж №42','Вст.№16',150,30,5);

INSERT INTO Boxes (Id_m,Id_o,Q_box,N_box) VALUES (1,1,20,15);
INSERT INTO Prods (Id_b,Id_d,RQ_box,P_date) VALUES (1,1,15,"14.12.2017");
COMMIT;