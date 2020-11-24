BEGIN TRANSACTION;
DROP TABLE IF EXISTS `Opers`;
CREATE TABLE IF NOT EXISTS `Opers` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`Opers`	TEXT
);
INSERT INTO `Opers` (_id,Opers) VALUES (1,'Производство'),
 (2,'Отгрузка'),
 (3,'Покраска');
DROP VIEW IF EXISTS `BoxesDone`;
CREATE VIEW BoxesDone AS Select Boxes.Q_box, sum(Prods.RQ_box) from Boxes b, Opers o, BoxMoves bm, Prods p where bm.Id_b=b._id and bm.Id_o=o._id and p.Id_bm=bm._id group by p.Id_bm;
COMMIT;
