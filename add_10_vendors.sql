-- =============================================================
-- 10 NEW VENDORS + MEDICINE ASSIGNMENTS
-- Multiple vendors sell same medicine at different prices
-- Designed to thoroughly test "Switch to Cheaper" logic
-- =============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ── STEP 1: ADD 10 NEW VENDOR ACCOUNTS ───────────────────────
INSERT INTO vendor_signup (id, username, email, mobileNo, password, role, verified) VALUES
(105, 'krishna_meds',   'krishna@meds.com',    '9811100001', 'pass123', 'vendor', 1),
(106, 'delhi_pharma',   'delhi@pharma.com',    '9811100002', 'pass123', 'vendor', 1),
(107, 'apollo_plus',    'apollo@plus.com',     '9811100003', 'pass123', 'vendor', 1),
(108, 'health_first',   'health@first.com',    '9811100004', 'pass123', 'vendor', 1),
(109, 'shree_medstore', 'shree@medstore.com',  '9811100005', 'pass123', 'vendor', 1),
(110, 'quickmeds',      'quick@meds.com',      '9811100006', 'pass123', 'vendor', 1),
(111, 'careplus',       'care@plus.com',       '9811100007', 'pass123', 'vendor', 1),
(112, 'jan_aushadhi',   'jan@aushadhi.com',    '9811100008', 'pass123', 'vendor', 1),
(113, 'netmeds_store',  'netmeds@store.com',   '9811100009', 'pass123', 'vendor', 1),
(114, 'pharmeasy_hub',  'pharmeasy@hub.com',   '9811100010', 'pass123', 'vendor', 1);

-- ── STEP 2: ADD VENDOR PROFILES ──────────────────────────────
INSERT INTO vendor_informations (vendor_user_id, ref_name, vendor_id, lat, lng, rating, reviews, delivery_time_minutes) VALUES
(105, 'Krishna Medicos',     105, 28.6200, 77.2100, 4.1, 280,  50),
(106, 'Delhi Pharma Hub',    106, 28.6350, 77.2250, 3.9, 190,  65),
(107, 'Apollo Plus Store',   107, 28.6450, 77.2050, 4.6, 620,  35),
(108, 'Health First Pharma', 108, 28.6550, 77.2150, 4.3, 410,  40),
(109, 'Shree Med Store',     109, 28.6100, 77.2300, 3.8, 150,  70),
(110, 'QuickMeds Express',   110, 28.6300, 77.2400, 4.7, 750,  20),
(111, 'Care Plus Pharmacy',  111, 28.6650, 77.2200, 4.4, 490,  45),
(112, 'Jan Aushadhi Kendra', 112, 28.6050, 77.2350, 4.0, 320,  55),
(113, 'NetMeds Store',       113, 28.6750, 77.2050, 4.5, 580,  30),
(114, 'PharmEasy Hub',       114, 28.6150, 77.2450, 4.2, 360,  48);

-- ── STEP 3: ASSIGN MEDICINES TO EACH VENDOR ──────────────────
-- vendor_medicine_id range: 2001–2070
-- Medicines used: 1(Augmentin), 2(Azithral), 3(Ascoril),
--                 4(Allegra 120), 5(Avil 25), 6(Amoxyclav)
-- Each medicine is sold by 6-8 vendors at different prices

INSERT INTO vendor_medicine
  (vendor_medicine_id, name, medicine_id, vendor_id, bucket_id,
   medicine_type, category, sub_category, salt_composition,
   packing_type, prescription_required, manufacture, country_of_origin, medicine_owner)
VALUES
-- ── Krishna Medicos (105): Augmentin, Azithral, Allegra ───
(2001, 'Augmentin 625 Duo Tablet', 1, 105, 1,  'Tablet', 'Antibiotic',  'Penicillin', 'Amoxicillin+Clavulanic Acid', 'Strip of 10',  1, 'GSK',      'India', 'vendor'),
(2002, 'Azithral 500 Tablet',      2, 105, 1,  'Tablet', 'Antibiotic',  'Macrolide',  'Azithromycin',               'Strip of 5',   1, 'Alembic',  'India', 'vendor'),
(2003, 'Allegra 120mg Tablet',     4, 105, 1,  'Tablet', 'Antiallergic','Antihistamine','Fexofenadine',             'Strip of 10',  0, 'Sanofi',   'India', 'vendor'),

-- ── Delhi Pharma Hub (106): Augmentin, Ascoril, Avil ──────
(2004, 'Augmentin 625 Duo Tablet', 1, 106, 4,  'Tablet', 'Antibiotic',  'Penicillin', 'Amoxicillin+Clavulanic Acid', 'Strip of 10', 1, 'GSK',      'India', 'vendor'),
(2005, 'Ascoril LS Syrup',         3, 106, 4,  'Syrup',  'Respiratory', 'Expectorant','Levosalbutamol+Ambroxol',    'Bottle 100ml',0, 'Glenmark', 'India', 'vendor'),
(2006, 'Avil 25 Tablet',           5, 106, 4,  'Tablet', 'Antiallergic','Antihistamine','Pheniramine',              'Strip of 10', 0, 'Sanofi',   'India', 'vendor'),

-- ── Apollo Plus Store (107): Azithral, Ascoril, Allegra, Avil
(2007, 'Azithral 500 Tablet',      2, 107, 5,  'Tablet', 'Antibiotic',  'Macrolide',  'Azithromycin',               'Strip of 5',  1, 'Alembic',  'India', 'vendor'),
(2008, 'Ascoril LS Syrup',         3, 107, 5,  'Syrup',  'Respiratory', 'Expectorant','Levosalbutamol+Ambroxol',   'Bottle 100ml',0, 'Glenmark', 'India', 'vendor'),
(2009, 'Allegra 120mg Tablet',     4, 107, 5,  'Tablet', 'Antiallergic','Antihistamine','Fexofenadine',             'Strip of 10', 0, 'Sanofi',   'India', 'vendor'),
(2010, 'Avil 25 Tablet',           5, 107, 5,  'Tablet', 'Antiallergic','Antihistamine','Pheniramine',              'Strip of 10', 0, 'Sanofi',   'India', 'vendor'),

-- ── Health First Pharma (108): ALL 6 medicines ────────────
(2011, 'Augmentin 625 Duo Tablet', 1, 108, 6,  'Tablet', 'Antibiotic',  'Penicillin', 'Amoxicillin+Clavulanic Acid', 'Strip of 10', 1, 'GSK',     'India', 'vendor'),
(2012, 'Azithral 500 Tablet',      2, 108, 6,  'Tablet', 'Antibiotic',  'Macrolide',  'Azithromycin',               'Strip of 5',  1, 'Alembic',  'India', 'vendor'),
(2013, 'Ascoril LS Syrup',         3, 108, 6,  'Syrup',  'Respiratory', 'Expectorant','Levosalbutamol+Ambroxol',   'Bottle 100ml',0, 'Glenmark', 'India', 'vendor'),
(2014, 'Allegra 120mg Tablet',     4, 108, 6,  'Tablet', 'Antiallergic','Antihistamine','Fexofenadine',             'Strip of 10', 0, 'Sanofi',   'India', 'vendor'),
(2015, 'Avil 25 Tablet',           5, 108, 6,  'Tablet', 'Antiallergic','Antihistamine','Pheniramine',              'Strip of 10', 0, 'Sanofi',   'India', 'vendor'),
(2016, 'Amoxyclav 625 Tablet',     6, 108, 6,  'Tablet', 'Antibiotic',  'Penicillin', 'Amoxicillin+Clavulanic Acid', 'Strip of 10', 1, 'Cipla',   'India', 'vendor'),

-- ── Shree Med Store (109): Augmentin, Amoxyclav ──────────
(2017, 'Augmentin 625 Duo Tablet', 1, 109, 1,  'Tablet', 'Antibiotic',  'Penicillin', 'Amoxicillin+Clavulanic Acid', 'Strip of 10', 1, 'GSK',     'India', 'vendor'),
(2018, 'Amoxyclav 625 Tablet',     6, 109, 1,  'Tablet', 'Antibiotic',  'Penicillin', 'Amoxicillin+Clavulanic Acid', 'Strip of 10', 1, 'Cipla',   'India', 'vendor'),

-- ── QuickMeds Express (110): ALL 6 medicines — cheapest combined ─
(2019, 'Augmentin 625 Duo Tablet', 1, 110, 4,  'Tablet', 'Antibiotic',  'Penicillin', 'Amoxicillin+Clavulanic Acid', 'Strip of 10', 1, 'GSK',     'India', 'vendor'),
(2020, 'Azithral 500 Tablet',      2, 110, 4,  'Tablet', 'Antibiotic',  'Macrolide',  'Azithromycin',               'Strip of 5',  1, 'Alembic',  'India', 'vendor'),
(2021, 'Ascoril LS Syrup',         3, 110, 4,  'Syrup',  'Respiratory', 'Expectorant','Levosalbutamol+Ambroxol',   'Bottle 100ml',0, 'Glenmark', 'India', 'vendor'),
(2022, 'Allegra 120mg Tablet',     4, 110, 4,  'Tablet', 'Antiallergic','Antihistamine','Fexofenadine',             'Strip of 10', 0, 'Sanofi',   'India', 'vendor'),
(2023, 'Avil 25 Tablet',           5, 110, 4,  'Tablet', 'Antiallergic','Antihistamine','Pheniramine',              'Strip of 10', 0, 'Sanofi',   'India', 'vendor'),
(2024, 'Amoxyclav 625 Tablet',     6, 110, 4,  'Tablet', 'Antibiotic',  'Penicillin', 'Amoxicillin+Clavulanic Acid', 'Strip of 10', 1, 'Cipla',   'India', 'vendor'),

-- ── Care Plus Pharmacy (111): Azithral, Allegra, Amoxyclav ──
(2025, 'Azithral 500 Tablet',      2, 111, 5,  'Tablet', 'Antibiotic',  'Macrolide',  'Azithromycin',               'Strip of 5',  1, 'Alembic',  'India', 'vendor'),
(2026, 'Allegra 120mg Tablet',     4, 111, 5,  'Tablet', 'Antiallergic','Antihistamine','Fexofenadine',             'Strip of 10', 0, 'Sanofi',   'India', 'vendor'),
(2027, 'Amoxyclav 625 Tablet',     6, 111, 5,  'Tablet', 'Antibiotic',  'Penicillin', 'Amoxicillin+Clavulanic Acid', 'Strip of 10', 1, 'Cipla',   'India', 'vendor'),

-- ── Jan Aushadhi Kendra (112): Avil, Ascoril, Augmentin ───
(2028, 'Augmentin 625 Duo Tablet', 1, 112, 6,  'Tablet', 'Antibiotic',  'Penicillin', 'Amoxicillin+Clavulanic Acid', 'Strip of 10', 1, 'GSK',     'India', 'vendor'),
(2029, 'Ascoril LS Syrup',         3, 112, 6,  'Syrup',  'Respiratory', 'Expectorant','Levosalbutamol+Ambroxol',   'Bottle 100ml',0, 'Glenmark', 'India', 'vendor'),
(2030, 'Avil 25 Tablet',           5, 112, 6,  'Tablet', 'Antiallergic','Antihistamine','Pheniramine',              'Strip of 10', 0, 'Sanofi',   'India', 'vendor'),

-- ── NetMeds Store (113): Azithral, Allegra, Ascoril ───────
(2031, 'Azithral 500 Tablet',      2, 113, 1,  'Tablet', 'Antibiotic',  'Macrolide',  'Azithromycin',               'Strip of 5',  1, 'Alembic',  'India', 'vendor'),
(2032, 'Allegra 120mg Tablet',     4, 113, 1,  'Tablet', 'Antiallergic','Antihistamine','Fexofenadine',             'Strip of 10', 0, 'Sanofi',   'India', 'vendor'),
(2033, 'Ascoril LS Syrup',         3, 113, 1,  'Syrup',  'Respiratory', 'Expectorant','Levosalbutamol+Ambroxol',   'Bottle 100ml',0, 'Glenmark', 'India', 'vendor'),

-- ── PharmEasy Hub (114): ALL 6 medicines ─────────────────
(2034, 'Augmentin 625 Duo Tablet', 1, 114, 4,  'Tablet', 'Antibiotic',  'Penicillin', 'Amoxicillin+Clavulanic Acid', 'Strip of 10', 1, 'GSK',     'India', 'vendor'),
(2035, 'Azithral 500 Tablet',      2, 114, 4,  'Tablet', 'Antibiotic',  'Macrolide',  'Azithromycin',               'Strip of 5',  1, 'Alembic',  'India', 'vendor'),
(2036, 'Ascoril LS Syrup',         3, 114, 4,  'Syrup',  'Respiratory', 'Expectorant','Levosalbutamol+Ambroxol',   'Bottle 100ml',0, 'Glenmark', 'India', 'vendor'),
(2037, 'Allegra 120mg Tablet',     4, 114, 4,  'Tablet', 'Antiallergic','Antihistamine','Fexofenadine',             'Strip of 10', 0, 'Sanofi',   'India', 'vendor'),
(2038, 'Avil 25 Tablet',           5, 114, 4,  'Tablet', 'Antiallergic','Antihistamine','Pheniramine',              'Strip of 10', 0, 'Sanofi',   'India', 'vendor'),
(2039, 'Amoxyclav 625 Tablet',     6, 114, 4,  'Tablet', 'Antibiotic',  'Penicillin', 'Amoxicillin+Clavulanic Acid', 'Strip of 10', 1, 'Cipla',   'India', 'vendor');

-- ── STEP 4: SET PRICES & STOCK ────────────────────────────────
-- Same medicine → different vendors → different prices
-- QuickMeds (110) is the "Super Vendor" — best combined price always

INSERT INTO vendor_medicine_price
  (vendor_medicine_id, vendor_id, mrp, selling_price, discount, quantity, expiry_date)
VALUES
-- Krishna Medicos (105)
(2001, 105, 180.00, 180.00, 0.0, 50,  '2027-06-01'),  -- Augmentin
(2002, 105, 118.00, 118.00, 0.0, 40,  '2027-06-01'),  -- Azithral
(2003, 105, 215.00, 215.00, 0.0, 60,  '2027-08-01'),  -- Allegra

-- Delhi Pharma Hub (106)
(2004, 106, 178.00, 178.00, 0.0, 35,  '2027-05-01'),  -- Augmentin
(2005, 106, 121.00, 121.00, 0.0, 45,  '2027-07-01'),  -- Ascoril
(2006, 106, 11.00,  11.00,  0.0, 100, '2027-09-01'),  -- Avil (expensive)

-- Apollo Plus Store (107)
(2007, 107, 116.00, 116.00, 0.0, 80,  '2027-06-01'),  -- Azithral (cheapest Azithral)
(2008, 107, 115.00, 115.00, 0.0, 70,  '2027-07-01'),  -- Ascoril (cheapest Ascoril)
(2009, 107, 210.00, 210.00, 0.0, 55,  '2027-08-01'),  -- Allegra
(2010, 107, 10.50,  10.50,  0.0, 90,  '2027-09-01'),  -- Avil (cheapest Avil)

-- Health First Pharma (108)
(2011, 108, 182.00, 182.00, 0.0, 60,  '2027-06-01'),  -- Augmentin
(2012, 108, 119.00, 119.00, 0.0, 50,  '2027-06-01'),  -- Azithral
(2013, 108, 119.00, 119.00, 0.0, 40,  '2027-07-01'),  -- Ascoril
(2014, 108, 218.00, 218.00, 0.0, 30,  '2027-08-01'),  -- Allegra
(2015, 108, 11.50,  11.50,  0.0, 80,  '2027-09-01'),  -- Avil
(2016, 108, 175.00, 175.00, 0.0, 45,  '2027-05-01'),  -- Amoxyclav

-- Shree Med Store (109)
(2017, 109, 185.00, 185.00, 0.0, 25,  '2027-04-01'),  -- Augmentin (expensive)
(2018, 109, 180.00, 180.00, 0.0, 20,  '2027-04-01'),  -- Amoxyclav (expensive)

-- QuickMeds Express (110) — BEST COMBINED PRICE (Switch to Cheaper target)
(2019, 110, 168.00, 168.00, 0.0, 300, '2027-12-01'),  -- Augmentin @ ₹168 (2nd cheapest per item)
(2020, 110, 113.00, 113.00, 0.0, 300, '2027-12-01'),  -- Azithral  @ ₹113 (2nd cheapest per item)
(2021, 110, 112.00, 112.00, 0.0, 300, '2027-12-01'),  -- Ascoril   @ ₹112 (2nd cheapest per item)
(2022, 110, 205.00, 205.00, 0.0, 300, '2027-12-01'),  -- Allegra   @ ₹205 (cheapest Allegra)
(2023, 110, 10.00,  10.00,  0.0, 300, '2027-12-01'),  -- Avil      @ ₹10  (cheapest Avil)
(2024, 110, 165.00, 165.00, 0.0, 300, '2027-12-01'),  -- Amoxyclav @ ₹165 (cheapest Amoxyclav)

-- Care Plus Pharmacy (111)
(2025, 111, 117.00, 117.00, 0.0, 55,  '2027-06-01'),  -- Azithral
(2026, 111, 212.00, 212.00, 0.0, 35,  '2027-08-01'),  -- Allegra
(2027, 111, 172.00, 172.00, 0.0, 40,  '2027-05-01'),  -- Amoxyclav

-- Jan Aushadhi Kendra (112)
(2028, 112, 162.00, 162.00, 0.0, 70,  '2027-06-01'),  -- Augmentin (CHEAPEST Augmentin!)
(2029, 112, 118.00, 118.00, 0.0, 60,  '2027-07-01'),  -- Ascoril
(2030, 112, 10.80,  10.80,  0.0, 120, '2027-09-01'),  -- Avil

-- NetMeds Store (113)
(2031, 113, 115.00, 115.00, 0.0, 65,  '2027-06-01'),  -- Azithral
(2032, 113, 208.00, 208.00, 0.0, 45,  '2027-08-01'),  -- Allegra
(2033, 113, 116.00, 116.00, 0.0, 55,  '2027-07-01'),  -- Ascoril

-- PharmEasy Hub (114)
(2034, 114, 174.00, 174.00, 0.0, 90,  '2027-06-01'),  -- Augmentin
(2035, 114, 120.00, 120.00, 0.0, 80,  '2027-06-01'),  -- Azithral
(2036, 114, 117.00, 117.00, 0.0, 75,  '2027-07-01'),  -- Ascoril
(2037, 114, 214.00, 214.00, 0.0, 50,  '2027-08-01'),  -- Allegra
(2038, 114, 10.70,  10.70,  0.0, 100, '2027-09-01'),  -- Avil
(2039, 114, 170.00, 170.00, 0.0, 65,  '2027-05-01');  -- Amoxyclav

SET FOREIGN_KEY_CHECKS = 1;

-- ── VERIFICATION ─────────────────────────────────────────────
SELECT 'Done! 10 vendors added.' as Status;

SELECT vm.name as Medicine,
       vi.ref_name as Vendor,
       vmp.mrp as Price,
       vmp.quantity as Stock
FROM vendor_medicine_price vmp
JOIN vendor_medicine vm ON vm.vendor_medicine_id = vmp.vendor_medicine_id
JOIN vendor_informations vi ON vi.vendor_user_id = vmp.vendor_id
WHERE vmp.vendor_id BETWEEN 105 AND 114
ORDER BY vm.name, vmp.mrp;
