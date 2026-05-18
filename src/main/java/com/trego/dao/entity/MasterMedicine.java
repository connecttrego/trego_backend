package com.trego.dao.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "medicine_master_db_table")
public class MasterMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Integer medicineId;

    @Column(name = "name", columnDefinition = "text")
    private String name;

    @Column(name = "category", length = 25)
    private String category;

    @Column(name = "sub_category", length = 25)
    private String subCategory;

    @Column(name = "manufacture", columnDefinition = "text")
    private String manufacture;

    @Column(name = "packaging", columnDefinition = "text")
    private String packaging;

    @Column(name = "pack_info", columnDefinition = "text")
    private String packInfo;

    @JsonIgnore
    @Column(name = "price")
    private Integer price;

    @JsonIgnore
    @Column(name = "discount_price", length = 25)
    private String discountPrice;

    @Column(name = "prescription_required", columnDefinition = "text")
    private String prescriptionRequired;

    @Column(name = "primary_use", columnDefinition = "text")
    private String primaryUse;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "salt_composition", columnDefinition = "text")
    private String saltComposition;

    @Column(name = "storage", columnDefinition = "text")
    private String storage;

    @Column(name = "introduction", columnDefinition = "text")
    private String introduction;

    @Column(name = "use_of", columnDefinition = "text")
    private String useOf;

    @Column(name = "benefits", columnDefinition = "text")
    private String benefits;

    @Column(name = "side_effect", columnDefinition = "text")
    private String sideEffect;

    @Column(name = "how_to_use", columnDefinition = "text")
    private String howToUse;

    @Column(name = "how_works", columnDefinition = "text")
    private String howWorks;

    @Column(name = "safety_advise", columnDefinition = "text")
    private String safetyAdvise;

    @Column(name = "if_miss", columnDefinition = "text")
    private String ifMiss;

    @Column(name = "ingredients", columnDefinition = "text")
    private String ingredients;

    @Column(name = "alternate_brand", columnDefinition = "text")
    private String alternateBrand;

    @Column(name = "manufacturer_address", columnDefinition = "text")
    private String manufacturerAddress;

    @Column(name = "for_sale", length = 50)
    private String forSale;

    @Column(name = "country_of_origin", length = 50)
    private String countryOfOrigin;

    @Column(name = "batch_id", length = 25)
    private String batchId;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "medicine_type", length = 25)
    private String medicineType;

    @Column(name = "packing_type", length = 50)
    private String packingType;

    @JsonIgnore
    @Column(name = "mrp", precision = 10, scale = 2)
    private BigDecimal mrp;

    @Column(name = "quantity")
    private Integer quantity;

    @JsonIgnore
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @JsonIgnore
    @Column(name = "selling_price", precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @JsonIgnore
    @Column(name = "offer_percent", precision = 5, scale = 2)
    private BigDecimal offerPercent;

    @Column(name = "expiry_date")
    @Temporal(TemporalType.DATE)
    private Date expiryDate;

    @Column(name = "how_it_works", columnDefinition = "longtext")
    private String howItWorks;

    @Column(name = "common_side_effect", columnDefinition = "longtext")
    private String commonSideEffect;

    @Column(name = "safety_advice", columnDefinition = "longtext")
    private String safetyAdvice;

    @Column(name = "alcohol_interaction")
    private String alcoholInteraction;

    @Column(name = "driving_interaction")
    private String drivingInteraction;

    @Column(name = "kidney_interaction")
    private String kidneyInteraction;

    @Column(name = "lactation_interaction")
    private String lactationInteraction;

    @Column(name = "liver_interaction")
    private String liverInteraction;

    @Column(name = "pregnancy_interaction")
    private String pregnancyInteraction;

    @Column(name = "question_answers", columnDefinition = "longtext")
    private String questionAnswers;

    @Column(name = "bucket_id")
    private Integer bucketId;

    @Column(name = "images", columnDefinition = "longtext")
    private String images;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "manufacturer_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date manufacturerDate;

    public String getPhoto1() {
        if (this.images == null || this.images.trim().isEmpty()) {
            return "";
        }
        String clean = this.images.trim();
        if (clean.startsWith("[") && clean.endsWith("]")) {
            int firstQuote = clean.indexOf("\"");
            if (firstQuote != -1) {
                int secondQuote = clean.indexOf("\"", firstQuote + 1);
                if (secondQuote != -1) {
                    return clean.substring(firstQuote + 1, secondQuote);
                }
            }
        }
        return this.images;
    }

    public String getImage() {
        return getPhoto1();
    }
}
