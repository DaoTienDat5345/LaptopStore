package com.example.stores.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Lớp wrapper để sử dụng với ComboBox cho loại bảo hành
 */
public class WarrantyTypeWrapper {
    private final StringProperty warrantyType = new SimpleStringProperty();

    public WarrantyTypeWrapper(String warrantyType) {
        this.warrantyType.set(warrantyType);
    }

    public String getWarrantyType() {
        return warrantyType.get();
    }

    public void setWarrantyType(String warrantyType) {
        this.warrantyType.set(warrantyType);
    }

    public StringProperty warrantyTypeProperty() {
        return warrantyType;
    }

    @Override
    public String toString() {
        return warrantyType.get();
    }
}