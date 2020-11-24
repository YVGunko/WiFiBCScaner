BEGIN TRANSACTION;
DROP TABLE IF EXISTS `Sotr`;
CREATE TABLE IF NOT EXISTS `Sotr` (
	`_id`	INTEGER PRIMARY KEY AUTOINCREMENT,
	`tn_Sotr`	TEXT,
	`Sotr`	TEXT
);
INSERT INTO `Sotr` (_id,tn_Sotr,Sotr) VALUES (1,'00000001','Чеботов'),
 (2,'00000002','Сапожинский'),
 (3,'00000003','Сандалкин');
DROP VIEW IF EXISTS `BoxesDone`;
CREATE VIEW BoxesDone AS Select Boxes.Q_box, sum(Prods.RQ_box) from Boxes b, Opers o, BoxMoves bm, Prods p where bm.Id_b=b._id and bm.Id_o=o._id and p.Id_bm=bm._id group by p.Id_bm;
COMMIT;
