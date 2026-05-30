-- =============================================================
-- TREGO DB - FRESH VENDOR SETUP
-- =============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ── STEP 1: DELETE OLD DATA ───────────────────────────────────
DELETE FROM vendor_medicine_price;
DELETE FROM vendor_medicine_information;
DELETE FROM vendor_discounts_offers;
DELETE FROM vendor_medicine;
DELETE FROM vendor_personal_details;
DELETE FROM vendor_application_status;
DELETE FROM vendor_informations;
DELETE FROM vendor_signup WHERE id IN (
  1,2,3,4,6,7,8,9,11,12,13,14,15,16,17,18,101,102,103,104,2016
);

-- Reuse existing bucket IDs (1, 4, 5, 6) for our vendors
-- bucket 1 → Raj Medicos, 4 → Sunita Pharma, 5 → City Health, 6 → MedEase

SET FOREIGN_KEY_CHECKS = 1;

-- ── STEP 2: ADD 4 FRESH VENDORS ───────────────────────────────
INSERT INTO vendor_signup (id, username, email, mobileNo, password, role, verified) VALUES
(101, 'rajmedicos',    'raj@medicos.com',    '9810011001', 'pass123', 'vendor', 1),
(102, 'sunita_pharma', 'sunita@pharma.com', '9820022002', 'pass123', 'vendor', 1),
(103, 'cityhealth',   'city@health.com',    '9830033003', 'pass123', 'vendor', 1),
(104, 'medease',      'medease@pharma.com', '9840044004', 'pass123', 'vendor', 1);

INSERT INTO vendor_informations (vendor_user_id, ref_name, vendor_id, lat, lng, rating, reviews, delivery_time_minutes) VALUES
(101, 'Raj Medicos',       101, 28.6139, 77.2090, 4.2, 320, 45),
(102, 'Sunita Pharma',     102, 28.6271, 77.2190, 4.0, 215, 60),
(103, 'City Health Store', 103, 28.6448, 77.2167, 4.5, 540, 30),
(104, 'MedEase Pharma',    104, 28.6502, 77.2301, 4.8, 890, 25);

-- ── STEP 3: ASSIGN MEDICINES (with bucket_id from existing bucket table) ──
-- Raj Medicos: Augmentin + Ascoril (no Azithral)
-- Sunita Pharma: Azithral + Ascoril (cheapest Azithral)
-- City Health: Augmentin only (cheapest Augmentin)
-- MedEase: ALL 3 — combined total is cheapest → triggers Switch to Cheaper

INSERT INTO vendor_medicine (vendor_medicine_id, name, medicine_id, vendor_id, bucket_id, medicine_type, category, sub_category, salt_composition, packing_type, prescription_required, manufacture, country_of_origin, medicine_owner) VALUES
-- Raj Medicos (101) — bucket_id=1
(1001, 'Augmentin 625 Duo Tablet', 1, 101, 1, 'Tablet', 'Antibiotic',  'Penicillin',  'Amoxicillin+Clavulanic Acid', 'Strip of 10',  1, 'GSK',       'India', 'vendor'),
(1002, 'Ascoril LS Syrup',         3, 101, 1, 'Syrup',  'Respiratory', 'Expectorant', 'Levosalbutamol+Ambroxol',     'Bottle 100ml', 0, 'Glenmark',  'India', 'vendor'),

-- Sunita Pharma (102) — bucket_id=4
(1003, 'Azithral 500 Tablet',      2, 102, 4, 'Tablet', 'Antibiotic',  'Macrolide',   'Azithromycin',                'Strip of 5',   1, 'Alembic',   'India', 'vendor'),
(1004, 'Ascoril LS Syrup',         3, 102, 4, 'Syrup',  'Respiratory', 'Expectorant', 'Levosalbutamol+Ambroxol',     'Bottle 100ml', 0, 'Glenmark',  'India', 'vendor'),

-- City Health Store (103) — bucket_id=5
(1005, 'Augmentin 625 Duo Tablet', 1, 103, 5, 'Tablet', 'Antibiotic',  'Penicillin',  'Amoxicillin+Clavulanic Acid', 'Strip of 10',  1, 'GSK',       'India', 'vendor'),

-- MedEase Pharma (104) — bucket_id=6, ALL 3 medicines
(1006, 'Augmentin 625 Duo Tablet', 1, 104, 6, 'Tablet', 'Antibiotic',  'Penicillin',  'Amoxicillin+Clavulanic Acid', 'Strip of 10',  1, 'GSK',       'India', 'vendor'),
(1007, 'Azithral 500 Tablet',      2, 104, 6, 'Tablet', 'Antibiotic',  'Macrolide',   'Azithromycin',                'Strip of 5',   1, 'Alembic',   'India', 'vendor'),
(1008, 'Ascoril LS Syrup',         3, 104, 6, 'Syrup',  'Respiratory', 'Expectorant', 'Levosalbutamol+Ambroxol',     'Bottle 100ml', 0, 'Glenmark',  'India', 'vendor');

-- ── STEP 4: SET PRICES & STOCK ────────────────────────────────
--
-- Price table:
--   Medicine        | Raj(101) | Sunita(102) | City(103) | MedEase(104)
--   ────────────────┼──────────┼─────────────┼───────────┼─────────────
--   Augmentin 625   |  175.00  |      —      | 165.00 ✓  |   172.00
--   Azithral 500    |    —     |  115.00  ✓  |     —     |   118.00
--   Ascoril LS Syr  |  120.00  |  122.00     |     —     |    98.00 ✓
--
-- User picks cheapest per medicine:
--   Augmentin → City Health   = ₹165
--   Azithral  → Sunita Pharma = ₹115
--   Ascoril   → Raj Medicos   = ₹120  (cheapest, but user picks Raj)
--   ─────────────────────────────────
--   TOTAL (3 vendors)         = ₹400
--
-- MedEase has ALL 3:  172 + 118 + 98 = ₹388
-- Savings = ₹400 - ₹388 = ₹12  → isCheaperOption: true ✓
--
-- Note: For Ascoril, MedEase (₹98) IS the cheapest,
--       but user still picks Raj (₹120) since they're browsing Raj.
--       Switch to Cheaper shows MedEase saves ₹12 overall.

INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, quantity, expiry_date) VALUES
-- Raj Medicos (101)
(1001, 101, 175.00, 175.00, 0.0, 80,  '2027-03-01'),
(1002, 101, 120.00, 120.00, 0.0, 60,  '2027-06-01'),

-- Sunita Pharma (102)
(1003, 102, 115.00, 115.00, 0.0, 100, '2027-04-01'),
(1004, 102, 122.00, 122.00, 0.0, 50,  '2027-06-01'),

-- City Health Store (103)
(1005, 103, 165.00, 165.00, 0.0, 120, '2027-05-01'),

-- MedEase Pharma (104) — combined cheapest
(1006, 104, 172.00, 172.00, 0.0, 200, '2027-07-01'),
(1007, 104, 118.00, 118.00, 0.0, 200, '2027-07-01'),
(1008, 104,  98.00,  98.00, 0.0, 200, '2027-07-01');

-- ── VERIFICATION OUTPUT ───────────────────────────────────────
SELECT '✓ Vendors created' as status;

SELECT vi.ref_name as Vendor, vm.name as Medicine, vmp.mrp as Price, vmp.quantity as Stock
FROM vendor_medicine_price vmp
JOIN vendor_medicine vm ON vm.vendor_medicine_id = vmp.vendor_medicine_id
JOIN vendor_informations vi ON vi.vendor_user_id = vmp.vendor_id
ORDER BY vm.name, vmp.mrp;

SELECT
  'User cart (3 vendors)' as scenario,
  CONCAT('₹', 165+115+120) as user_pays,
  CONCAT('₹', 172+118+98) as medease_total,
  CONCAT('₹', (165+115+120)-(172+118+98)) as savings;
