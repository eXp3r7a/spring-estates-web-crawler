package com.example.spring_web_crawler_demo.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "estates")
public class Estate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estate_id")
    private Long id;

    private String title;
    private String area;
    private int yearOfConstruction;
    private int floor;
    private String location;
    private String price;
    private String listingUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public int getYearOfConstruction() {
        return yearOfConstruction;
    }

    public void setYearOfConstruction(int yearOfConstruction) {
        this.yearOfConstruction = yearOfConstruction;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getListingUrl() {
        return listingUrl;
    }

    public void setListingUrl(String listingUrl) {
        this.listingUrl = listingUrl;
    }

    @Override
    public String toString() {
        return "Estate{" +
                "title='" + title + '\'' +
                ", area='" + area + '\'' +
                ", yearOfConstruction=" + yearOfConstruction +
                ", floor=" + floor +
                ", location='" + location + '\'' +
                ", price='" + price + '\'' +
                ", publishedFrom='" + listingUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Estate estate = (Estate) o;
        return yearOfConstruction == estate.yearOfConstruction && floor == estate.floor && Objects.equals(id, estate.id) && Objects.equals(title, estate.title) && Objects.equals(area, estate.area) && Objects.equals(location, estate.location) && Objects.equals(price, estate.price) && Objects.equals(listingUrl, estate.listingUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, area, yearOfConstruction, floor, location, price, listingUrl);
    }
}
