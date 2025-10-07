package com.trego.utils;

import com.trego.dao.entity.Medicine;
import com.trego.dao.entity.Stock;
import com.trego.dao.entity.Vendor;
import com.trego.dao.impl.MedicineRepository;
import com.trego.dao.impl.StockRepository;
import com.trego.dao.impl.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Test data seeder for bucket optimization API testing
 * This class seeds test data to help with testing the bucket optimization functionality
 */
@Component
public class BucketTestDataSeeder implements CommandLineRunner {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private StockRepository stockRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if we already have data
        if (medicineRepository.count() > 0) {
            return; // Data already exists, don't seed
        }

        // Create test vendors
        Vendor vendor1 = new Vendor();
        vendor1.setName("PharmaPlus");
        vendor1.setDruglicense("DL12345");
        vendor1.setGistin("GST12345");
        vendor1.setCategory("retail");
        vendor1.setLogo("logo1.png");
        vendor1.setLat("12.9716");
        vendor1.setLng("77.5946");
        vendor1.setAddress("Bangalore, Karnataka");
        vendor1.setDeliveryTime("1-2 days");
        vendor1.setReviews("4.5/5");

        Vendor vendor2 = new Vendor();
        vendor2.setName("MediCare");
        vendor2.setDruglicense("DL67890");
        vendor2.setGistin("GST67890");
        vendor2.setCategory("online");
        vendor2.setLogo("logo2.png");
        vendor2.setLat("13.0827");
        vendor2.setLng("80.2707");
        vendor2.setAddress("Chennai, Tamil Nadu");
        vendor2.setDeliveryTime("2-3 days");
        vendor2.setReviews("4.2/5");

        Vendor vendor3 = new Vendor();
        vendor3.setName("HealthFirst");
        vendor3.setDruglicense("DL11111");
        vendor3.setGistin("GST11111");
        vendor3.setCategory("retail");
        vendor3.setLogo("logo3.png");
        vendor3.setLat("15.2993");
        vendor3.setLng("74.1240");
        vendor3.setAddress("Goa");
        vendor3.setDeliveryTime("Same day");
        vendor3.setReviews("4.8/5");

        List<Vendor> vendors = Arrays.asList(vendor1, vendor2, vendor3);
        vendorRepository.saveAll(vendors);

        // Create test medicines
        Medicine medicine1 = new Medicine();
        medicine1.setName("Paracetamol 500mg");
        medicine1.setManufacturer("Cipla");
        medicine1.setSaltComposition("Paracetamol");
        medicine1.setMedicineType("Tablet");
        medicine1.setIntroduction("Used for fever and pain relief");
        medicine1.setDescription("Paracetamol is used to treat many conditions such as headache, muscle aches, arthritis, backache, toothaches, colds, and fevers.");
        medicine1.setPacking("15 tablets");

        Medicine medicine2 = new Medicine();
        medicine2.setName("Amoxicillin 500mg");
        medicine2.setManufacturer("Sun Pharma");
        medicine2.setSaltComposition("Amoxicillin");
        medicine2.setMedicineType("Capsule");
        medicine2.setIntroduction("Used to treat bacterial infections");
        medicine2.setDescription("Amoxicillin is used to treat many different types of infection caused by bacteria, such as tonsillitis, bronchitis, pneumonia, and infections of the ear, nose, throat, skin, or urinary tract.");
        medicine2.setPacking("10 capsules");

        Medicine medicine3 = new Medicine();
        medicine3.setName("Omeprazole 20mg");
        medicine3.setManufacturer("Dr. Reddy's");
        medicine3.setSaltComposition("Omeprazole");
        medicine3.setMedicineType("Capsule");
        medicine3.setIntroduction("Used to treat stomach acid related problems");
        medicine3.setDescription("Omeprazole is used to treat certain stomach and esophagus problems (such as acid reflux, ulcers). It works by decreasing the amount of acid your stomach makes.");
        medicine3.setPacking("14 capsules");

        List<Medicine> medicines = Arrays.asList(medicine1, medicine2, medicine3);
        medicineRepository.saveAll(medicines);

        // Create test stocks
        // Vendor 1 stocks
        Stock stock1v1 = new Stock();
        stock1v1.setMedicine(medicine1);
        stock1v1.setVendor(vendor1);
        stock1v1.setMrp(15.0);
        stock1v1.setDiscount(10.0);
        stock1v1.setQty(100);

        Stock stock2v1 = new Stock();
        stock2v1.setMedicine(medicine2);
        stock2v1.setVendor(vendor1);
        stock2v1.setMrp(50.0);
        stock2v1.setDiscount(5.0);
        stock2v1.setQty(50);

        Stock stock3v1 = new Stock();
        stock3v1.setMedicine(medicine3);
        stock3v1.setVendor(vendor1);
        stock3v1.setMrp(75.0);
        stock3v1.setDiscount(15.0);
        stock3v1.setQty(30);

        // Vendor 2 stocks
        Stock stock1v2 = new Stock();
        stock1v2.setMedicine(medicine1);
        stock1v2.setVendor(vendor2);
        stock1v2.setMrp(12.0);
        stock1v2.setDiscount(15.0);
        stock1v2.setQty(200);

        Stock stock2v2 = new Stock();
        stock2v2.setMedicine(medicine2);
        stock2v2.setVendor(vendor2);
        stock2v2.setMrp(45.0);
        stock2v2.setDiscount(10.0);
        stock2v2.setQty(75);

        Stock stock3v2 = new Stock();
        stock3v2.setMedicine(medicine3);
        stock3v2.setVendor(vendor2);
        stock3v2.setMrp(70.0);
        stock3v2.setDiscount(20.0);
        stock3v2.setQty(40);

        // Vendor 3 stocks (only has medicine 1 and 2)
        Stock stock1v3 = new Stock();
        stock1v3.setMedicine(medicine1);
        stock1v3.setVendor(vendor3);
        stock1v3.setMrp(18.0);
        stock1v3.setDiscount(5.0);
        stock1v3.setQty(150);

        Stock stock2v3 = new Stock();
        stock2v3.setMedicine(medicine2);
        stock2v3.setVendor(vendor3);
        stock2v3.setMrp(55.0);
        stock2v3.setDiscount(8.0);
        stock2v3.setQty(60);

        List<Stock> stocks = Arrays.asList(
                stock1v1, stock2v1, stock3v1,
                stock1v2, stock2v2, stock3v2,
                stock1v3, stock2v3
        );
        stockRepository.saveAll(stocks);

        System.out.println("Test data seeded successfully for bucket optimization API testing!");
    }
}