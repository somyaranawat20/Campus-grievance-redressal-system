package com.inn.cgrs.model;

public enum GrievanceCategory {
    ELECTRICAL("Electrical Issues", "Lights, fans, switches, wiring"),
    COMPUTER_HARDWARE("Computer Hardware", "Keyboard, monitor, CPU, mouse"),
    PROJECTORS("Projectors & Classroom Equipment", "Projectors and classroom equipment"),
    FURNITURE("Furniture Issues", "Broken chairs, damaged tables, benches"),
    LIBRARY_FACULTY("Library & Faculty Room", "Library and faculty room infrastructure"),
    WATER_SUPPLY("Water Supply & Facilities", "Water supply and basic campus facilities");
    
    private final String displayName;
    private final String description;
    
    GrievanceCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
