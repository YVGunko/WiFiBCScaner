BEGIN TRANSACTION;
DROP TABLE IF EXISTS `Deps`;
CREATE TABLE IF NOT EXISTS `Deps` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`Id_deps`	TEXT,
	`Name_Deps`	TEXT
);
INSERT INTO `Deps` (_id,Id_deps,Name_Deps) VALUES (1,'000000020','Бригада ТЭП №1 Каблуки'),
 (2,'000000027','Бригада ТЭП №1 Ранты'),
 (3,'000000001','Бригада ТЭП №1 Стойки'),
 (4,'000000013','Бригада ТЭП №1 Тройки'),
 (5,'000000014','Бригада ТЭП №1 Цвет'),
 (6,'000000019','Бригада ТЭП №2 Каблуки'),
 (7,'000000028','Бригада ТЭП №2 Ранты'),
 (8,'000000004','Бригада ТЭП №2 Стойки'),
 (9,'000000015','Бригада ТЭП №2 Тройки'),
 (10,'000000016','Бригада ТЭП №2 Цвет');
DROP VIEW IF EXISTS `BoxesDone`;
CREATE VIEW BoxesDone AS Select Boxes.Q_box, sum(Prods.RQ_box) from Boxes b, Opers o, BoxMoves bm, Prods p where bm.Id_b=b._id and bm.Id_o=o._id and p.Id_bm=bm._id group by p.Id_bm;
COMMIT;
