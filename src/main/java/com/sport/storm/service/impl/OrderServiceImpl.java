package com.sport.storm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sport.storm.entity.*;
import com.sport.storm.mapper.*;
import com.sport.storm.service.MemberService;
import com.sport.storm.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final ShippingMapper shippingMapper;
    private final ShippingTraceMapper shippingTraceMapper;
    private final UserMapper userMapper;
    private final MemberService memberService;
    private final UserBehaviorMapper userBehaviorMapper;

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                            CartItemMapper cartItemMapper, ProductMapper productMapper,
                            AddressMapper addressMapper, ShippingMapper shippingMapper,
                            ShippingTraceMapper shippingTraceMapper, UserMapper userMapper,
                            MemberService memberService, UserBehaviorMapper userBehaviorMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.addressMapper = addressMapper;
        this.shippingMapper = shippingMapper;
        this.shippingTraceMapper = shippingTraceMapper;
        this.userMapper = userMapper;
        this.memberService = memberService;
        this.userBehaviorMapper = userBehaviorMapper;
    }

    @Override
    @Transactional
    public Order createFromCart(Long userId, Long addressId, Long[] cartItemIds) {
        // 1. 校验收货地址归属
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("收货地址不存在");
        }

        // 2. 取购物车选中项
        List<CartItem> cartItems = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .in(CartItem::getId, java.util.Arrays.asList(cartItemIds))
                        .eq(CartItem::getSelected, 1));
        if (cartItems.isEmpty()) {
            throw new RuntimeException("未选择要结算的商品");
        }

        // 3. 生成订单主表
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setReceiver(address.getReceiver());
        order.setPhone(address.getPhone());
        order.setAddressDetail(address.getProvince() + address.getCity()
                + address.getDistrict() + address.getDetail());
        order.setStatus(0); // 待付款

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 4. 校验库存、算金额、写明细、扣库存
        for (CartItem ci : cartItems) {
            Product p = productMapper.selectById(ci.getProductId());
            if (p == null) {
                throw new RuntimeException("商品不存在：" + ci.getProductId());
            }
            if (p.getStock() < ci.getQuantity()) {
                throw new RuntimeException("商品[" + p.getName() + "]库存不足");
            }
            // 扣库存
            p.setStock(p.getStock() - ci.getQuantity());
            p.setSales(p.getSales() + ci.getQuantity());
            productMapper.updateById(p);

            BigDecimal itemTotal = p.getPrice().multiply(new BigDecimal(ci.getQuantity()));
            total = total.add(itemTotal);

            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setProductImage(p.getImage());
            oi.setPrice(p.getPrice());
            oi.setQuantity(ci.getQuantity());
            oi.setTotalPrice(itemTotal);
            orderItems.add(oi);
        }

        // 5. 会员折扣（按用户等级）
        BigDecimal discountRate = new BigDecimal("1.00");
        User user = userMapper.selectById(userId);
        if (user != null && user.getMemberLevelId() != null) {
            MemberLevel level = memberService.calcLevel(user.getTotalExpend());
            if (level != null && level.getDiscount() != null) {
                discountRate = level.getDiscount();
            }
        }

        // 6. 计算折扣后的实付金额
        BigDecimal pay = total.multiply(discountRate)
                .setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal discount = total.subtract(pay);

        order.setTotalAmount(total);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setDiscountAmount(discount);
        order.setPayAmount(pay);
        order.setCreateTime(LocalDateTime.now());
        orderMapper.insert(order);

        // 7. 写订单明细
        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }

        // 8. 删除已结算的购物车项
        cartItemMapper.delete(
                new LambdaQueryWrapper<CartItem>()
                        .eq(CartItem::getUserId, userId)
                        .in(CartItem::getId, java.util.Arrays.asList(cartItemIds)));

        order.setItems(orderItems);
        return order;
    }

    @Override
    public Order getDetail(Long userId, Long orderId) {
        Order order = getOwned(userId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        order.setItems(items);

        // 带出物流信息
        Shipping shipping = shippingMapper.selectOne(
                new LambdaQueryWrapper<Shipping>().eq(Shipping::getOrderId, orderId));
        if (shipping != null) {
            List<ShippingTrace> traces = shippingTraceMapper.selectList(
                    new LambdaQueryWrapper<ShippingTrace>()
                            .eq(ShippingTrace::getShippingId, shipping.getId())
                            .orderByAsc(ShippingTrace::getTraceTime));
            shipping.setTraces(traces);
            order.setShipping(shipping);
        }
        return order;
    }

    @Override
    public Page<Order> myOrders(Long userId, long current, long size, Integer status) {
        Page<Order> page = new Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> result = orderMapper.selectPage(page, wrapper);

        // 为每条订单填充商品明细
        for (Order o : result.getRecords()) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
            o.setItems(items);
        }
        return result;
    }

    @Override
    public Page<Order> adminListAll(long current, long size, Integer status) {
        Page<Order> page = new Page<>(current, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreateTime);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> result = orderMapper.selectPage(page, wrapper);
        for (Order o : result.getRecords()) {
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
            o.setItems(items);
        }
        return result;
    }

    @Override
    @Transactional
    public void pay(Long userId, Long orderId) {
        Order order = getOwned(userId, orderId);
        if (order.getStatus() != 0) {
            throw new RuntimeException("当前状态不可支付");
        }
        order.setStatus(1); // 待发货
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 累计消费额、发放积分、刷新会员等级
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setTotalExpend(user.getTotalExpend().add(order.getPayAmount()));
            userMapper.updateById(user);
        }
        memberService.addPoints(userId, order.getPayAmount().intValue(), "订单支付");
        memberService.refreshLevel(userId);

        // 记录购买行为，供推荐模块使用
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem oi : items) {
            UserBehavior behavior = new UserBehavior();
            behavior.setUserId(userId);
            behavior.setProductId(oi.getProductId());
            behavior.setBehaviorType(5); // 5=购买
            behavior.setBehaviorTime(LocalDateTime.now());
            userBehaviorMapper.insert(behavior);
        }
    }

    @Override
    @Transactional
    public void ship(Long orderId, String expressCompany, String expressNo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new RuntimeException("当前状态不可发货");
        }
        order.setStatus(2); // 运输中
        order.setShipTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 建物流单 + 初始轨迹
        Shipping shipping = new Shipping();
        shipping.setOrderId(orderId);
        shipping.setExpressCompany(expressCompany);
        shipping.setExpressNo(expressNo);
        shipping.setStatus(1);
        shipping.setUpdateTime(LocalDateTime.now());
        shippingMapper.insert(shipping);

        ShippingTrace trace = new ShippingTrace();
        trace.setShippingId(shipping.getId());
        trace.setTraceInfo("【" + expressCompany + "】已揽收，快件已从发货地发出");
        trace.setTraceTime(LocalDateTime.now());
        shippingTraceMapper.insert(trace);
    }

    @Override
    @Transactional
    public void confirm(Long userId, Long orderId) {
        Order order = getOwned(userId, orderId);
        if (order.getStatus() != 2) {
            throw new RuntimeException("当前状态不可确认收货");
        }
        order.setStatus(3); // 已完成
        order.setFinishTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long orderId) {
        Order order = getOwned(userId, orderId);
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            throw new RuntimeException("当前状态不可取消");
        }
        order.setStatus(4); // 已取消
        order.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(order);

        // 取消后回滚库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        for (OrderItem oi : items) {
            Product p = productMapper.selectById(oi.getProductId());
            if (p != null) {
                p.setStock(p.getStock() + oi.getQuantity());
                p.setSales(Math.max(0, p.getSales() - oi.getQuantity()));
                productMapper.updateById(p);
            }
        }
    }

    private Order getOwned(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        return order;
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}