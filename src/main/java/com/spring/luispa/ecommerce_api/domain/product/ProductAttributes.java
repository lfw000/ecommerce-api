package com.spring.luispa.ecommerce_api.domain.product;

import java.util.HashMap;
import java.util.Map;

public class ProductAttributes extends HashMap<String, String> {

    //private Map<String, String> values = new HashMap<>();
    //
    //public ProductAttributes() {
    //    // No-args constructor
    //}
    //
    //public ProductAttributes(Map<String, String> attributes) {
    //    this.values = attributes;
    //}
    //
    //public void addAttribute(String key, String value) {
    //    values.put(key, value);
    //}
    //
    //public String getAttribute(String key) {
    //    return values.get(key);
    //}
    //
    //public Map<String, String> getValues() {
    //    return values;
    //}
    //
    //public void setValues(Map<String, String> values) {
    //    this.values = values;
    //}

    public ProductAttributes() {
        super();
    }

    public ProductAttributes(Map<String, String> map) {
        super(map);
    }

    public void addAttribute(String key, String value) {
        put(key, value);
    }

    public  String getAttribute(String key) {
        return get(key);
    }
}
