const fs = require('fs');

let sql = `-- Switch To Cheaper Optimization Test Case
USE tredo_db_2;
`;

// Insert 16 Vendors (1001 to 1016)
sql += `\n-- 1. Insert Dummy Vendors\n`;
for (let i = 1; i <= 16; i++) {
    const isSuper = i === 16;
    const vendorName = isSuper ? 'Super Vendor V4' : \`Competition Vendor \${i}\`;
    sql += `INSERT IGNORE INTO vendor_informations (vendor_user_id, ref_name, delivery_time_minutes) VALUES (1000 + ${i}, '${vendorName}', '60');\n`;
}

let bucketId = 1;
const medicines = [
    { id: 1, name: 'Augmentin 625 Duo Tablet' },
    { id: 2, name: 'Azithral 500 Tablet' },
    { id: 3, name: 'Ascoril LS Syrup' }
];

let vendorOffset = 1; // 1 to 5 for M1, 6 to 10 for M2, 11 to 15 for M3
let prices = [150, 140, 130, 120, 100]; // 5th vendor will always be cheapest

sql += `\n-- 2. Insert Vendor Medicine and Prices\n`;

for (let m = 0; m < medicines.length; m++) {
    const med = medicines[m];
    
    // 5 Competing Vendors for this medicine
    for (let i = 0; i < 5; i++) {
        const vendorId = 1000 + vendorOffset;
        const price = prices[i];
        
        sql += `INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('${med.name}', ${med.id}, ${vendorId}, ${bucketId});\n`;
        sql += `SET @last_vm_id = LAST_INSERT_ID();\n`;
        sql += `INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@last_vm_id, ${vendorId}, ${price + 20}, ${price}, 0, 50);\n`;
        sql += `SET @last_price_id = LAST_INSERT_ID();\n`;
        sql += `UPDATE vendor_medicine SET price_id = @last_price_id WHERE vendor_medicine_id = @last_vm_id;\n`;
        
        vendorOffset++;
    }
}

// 3. Super Vendor (Vendor 1016) has ALL 3 medicines at 90 Rs each
const superVendorId = 1016;
for (let m = 0; m < medicines.length; m++) {
    const med = medicines[m];
    sql += `INSERT INTO vendor_medicine (name, medicine_id, vendor_id, bucket_id) VALUES ('${med.name}', ${med.id}, ${superVendorId}, ${bucketId});\n`;
    sql += `SET @last_vm_id = LAST_INSERT_ID();\n`;
    sql += `INSERT INTO vendor_medicine_price (vendor_medicine_id, vendor_id, mrp, selling_price, discount, qty) VALUES (@last_vm_id, ${superVendorId}, 110, 90, 0, 50);\n`;
    sql += `SET @last_price_id = LAST_INSERT_ID();\n`;
    sql += `UPDATE vendor_medicine SET price_id = @last_price_id WHERE vendor_medicine_id = @last_vm_id;\n`;
}

fs.writeFileSync('insert_optimization.sql', sql);
console.log('SQL script generated successfully in insert_optimization.sql');
