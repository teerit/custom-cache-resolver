package com.example.spring.cache.demo.controller;

import com.example.spring.cache.demo.model.Country;
import com.example.spring.cache.demo.model.Province;
import com.example.spring.cache.demo.repository.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CountryController {

    @Autowired
    CountryRepository countryRepository;

    @GetMapping(value = "/country/{code}")
    public ResponseEntity<Country> getCountryByCode(@PathVariable("code") String code) {
        Country country = countryRepository.findByCountryCode(code);
        return ResponseEntity.ok(country);
    }

    @GetMapping(value = "/province/{code}")
    public ResponseEntity<Province> getProvinceCode(@PathVariable("code") String code) {
        Province province = countryRepository.findByProvinceCode(code);
        return ResponseEntity.ok(province);
    }


    @DeleteMapping(value = "/country/{code}")
    public ResponseEntity deleteCountryByCode(@PathVariable("code") String code) {
        countryRepository.deleteCode(code);
        return ResponseEntity.ok().build();
    }
}