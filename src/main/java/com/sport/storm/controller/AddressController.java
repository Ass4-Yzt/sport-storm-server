package com.sport.storm.controller;

import com.sport.storm.common.Result;
import com.sport.storm.entity.Address;
import com.sport.storm.service.AddressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    private Long userId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    @GetMapping("/list")
    public Result<List<Address>> list(HttpServletRequest request) {
        return Result.success(addressService.list(userId(request)));
    }

    @PostMapping("/add")
    public Result<Void> add(HttpServletRequest request, @RequestBody Address address) {
        addressService.add(userId(request), address);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(HttpServletRequest request, @RequestBody Address address) {
        addressService.update(userId(request), address);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        addressService.delete(userId(request), id);
        return Result.success();
    }

    @PutMapping("/default/{id}")
    public Result<Void> setDefault(HttpServletRequest request, @PathVariable Long id) {
        addressService.setDefault(userId(request), id);
        return Result.success();
    }
}