USE tredo_db_2;

-- 1. Dummy Vendors for Medicine 1 (M1 - Augmentin 625 Duo Tablet)
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2001, 'V1 M1 150Rs', '60');
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2002, 'V2 M1 140Rs', '60');
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2003, 'V3 M1 130Rs', '60');
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2004, 'V4 M1 120Rs', '60');
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2005, 'V5 M1 Cheapest 100Rs', '60');

-- Medicine 1 Stocks
INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Augmentin 625 Duo Tablet', 1, 2001, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2001, 170, 150, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Augmentin 625 Duo Tablet', 1, 2002, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2002, 160, 140, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Augmentin 625 Duo Tablet', 1, 2003, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2003, 150, 130, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Augmentin 625 Duo Tablet', 1, 2004, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2004, 140, 120, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Augmentin 625 Duo Tablet', 1, 2005, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2005, 120, 100, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;


-- 2. Dummy Vendors for Medicine 2 (M2 - Azithral 500 Tablet)
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2006, 'V1 M2 150Rs', '60');
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2007, 'V2 M2 140Rs', '60');
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2008, 'V3 M2 130Rs', '60');
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2009, 'V4 M2 120Rs', '60');
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2010, 'V5 M2 Cheapest 100Rs', '60');

-- Medicine 2 Stocks
INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Azithral 500 Tablet', 2, 2006, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2006, 170, 150, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Azithral 500 Tablet', 2, 2007, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2007, 160, 140, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Azithral 500 Tablet', 2, 2008, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2008, 150, 130, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Azithral 500 Tablet', 2, 2009, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2009, 140, 120, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Azithral 500 Tablet', 2, 2010, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2010, 120, 100, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;


-- 3. Dummy Vendors for Medicine 3 (M3 - Ascoril LS Syrup)
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2011, 'V1 M3 150Rs', '60');
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2012, 'V2 M3 140Rs', '60');
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2013, 'V3 M3 130Rs', '60');
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2014, 'V4 M3 120Rs', '60');
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2015, 'V5 M3 Cheapest 100Rs', '60');

-- Medicine 3 Stocks
INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Ascoril LS Syrup', 3, 2011, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2011, 170, 150, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Ascoril LS Syrup', 3, 2012, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2012, 160, 140, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Ascoril LS Syrup', 3, 2013, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2013, 150, 130, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Ascoril LS Syrup', 3, 2014, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2014, 140, 120, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Ascoril LS Syrup', 3, 2015, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2015, 120, 100, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;


-- 4. SUPER VENDOR (V_super)
INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (2016, 'SUPER VENDOR V_super', '60');

-- Super Vendor Stocks for all 3 medicines at 90 Rs each
INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Augmentin 625 Duo Tablet', 1, 2016, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2016, 110, 90, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Azithral 500 Tablet', 2, 2016, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2016, 110, 90, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;

INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('Ascoril LS Syrup', 3, 2016, 1);
SET @vm_id = LAST_INSERT_ID(); INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@vm_id, 2016, 110, 90, 0, 50); UPDATE vendor_medicine SET price_id = LAST_INSERT_ID() WHERE vendor_medicine_id = @vm_id;
