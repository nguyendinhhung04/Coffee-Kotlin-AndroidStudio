{
"_id": "promo_combo_breakfast",
"name": "Combo Cà phê Sữa Đá + Croissant",
"description": "Combo bữa sáng 49k",
"type": "FIXED_PRICE_COMBO",        // PERCENT | FIXED_AMOUNT | FIXED_PRICE_COMBO
"scope": "COMBO",                   // ORDER | PRODUCT | CATEGORY | COMBO
"value": 49000,                     // tuỳ type
"startDate": ISODate("2025-12-01T00:00:00Z"),
"endDate": ISODate("2026-01-01T00:00:00Z"),
"minOrderTotal": null,
"isActive": true,

// áp dụng cho từng loại scope
"productIds": [                     // cho scope = PRODUCT
"6924a37bd69221014869c607"
],
"categories": [                     // cho scope = CATEGORY
"chocolate"
],
"comboItems": [                     // cho scope = COMBO
{ "productId": "6924a37bd69221014869c603", "requiredQty": 1 },
{ "productId": "700000000000000000000002", "requiredQty": 1 }
]
}

order thêm attribute
"appliedPromotions": [
{
"promotionId": "promo_combo_breakfast",
"discountAmount": 5000
}
],