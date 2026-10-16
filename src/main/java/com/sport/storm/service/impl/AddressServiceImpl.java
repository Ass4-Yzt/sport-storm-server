package com.sport.storm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sport.storm.entity.Address;
import com.sport.storm.mapper.AddressMapper;
import com.sport.storm.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    public AddressServiceImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public List<Address> list(Long userId) {
        return addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault));
    }

    @Override
    @Transactional
    public void add(Long userId, Address address) {
        address.setId(null);
        address.setUserId(userId);
        // 第一个地址设为默认
        Long count = addressMapper.selectCount(
                new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
        if (count == 0) {
            address.setIsDefault(1);
        } else if (address.getIsDefault() == null || address.getIsDefault() == 0) {
            address.setIsDefault(0);
        }
        addressMapper.insert(address);

        // 如果设为默认，取消其他默认
        if (address.getIsDefault() == 1) {
            clearOtherDefault(userId, address.getId());
        }
    }

    @Override
    @Transactional
    public void update(Long userId, Address address) {
        Address exist = getOwned(userId, address.getId());
        address.setUserId(userId);
        addressMapper.updateById(address);
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            clearOtherDefault(userId, address.getId());
        }
    }

    @Override
    public void delete(Long userId, Long id) {
        addressMapper.delete(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getId, id)
                        .eq(Address::getUserId, userId));
    }

    @Override
    @Transactional
    public void setDefault(Long userId, Long id) {
        getOwned(userId, id);
        clearOtherDefault(userId, id);
        Address a = new Address();
        a.setId(id);
        a.setIsDefault(1);
        addressMapper.updateById(a);
    }

    private Address getOwned(Long userId, Long id) {
        Address a = addressMapper.selectById(id);
        if (a == null || !a.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在");
        }
        return a;
    }

    private void clearOtherDefault(Long userId, Long exceptId) {
        List<Address> others = addressMapper.selectList(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .ne(Address::getId, exceptId));
        for (Address a : others) {
            if (a.getIsDefault() != null && a.getIsDefault() == 1) {
                a.setIsDefault(0);
                addressMapper.updateById(a);
            }
        }
    }
}