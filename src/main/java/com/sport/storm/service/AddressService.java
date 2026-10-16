package com.sport.storm.service;

import com.sport.storm.entity.Address;
import java.util.List;

public interface AddressService {
    List<Address> list(Long userId);
    void add(Long userId, Address address);
    void update(Long userId, Address address);
    void delete(Long userId, Long id);
    void setDefault(Long userId, Long id);
}