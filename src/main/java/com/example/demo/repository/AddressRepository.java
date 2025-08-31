package com.example.demo.repository;

import com.example.demo.model.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends MongoRepository<Address, String> {
    List<Address> findByUserId(String userId);
}