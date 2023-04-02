package com.sushishop.scheduler;

import com.sushishop.Constant;
import com.sushishop.entity.SushiOrder;
import com.sushishop.repository.SushiOrderRepository;
import com.sushishop.service.CachedService;

public class Chef{

    private Integer chefId;

    private boolean idle;

    private Long lastUpdateTime;

    private Long timeToMake;

    private Long timeSpent;

    private Long orderId;

    
    public Chef(Integer chefId, boolean idle, Long lastUpdateTime, Long timeToMake, Long timeSpent, Long orderId) {
        this.chefId = chefId;
        this.idle = idle;
        this.lastUpdateTime = lastUpdateTime;
        this.timeToMake = timeToMake;
        this.timeSpent = timeSpent;
        this.orderId = orderId;
    }

    public Chef(Integer chefId){
        this.chefId = chefId;
        this.idle = true;
        this.lastUpdateTime = 0L;
        this.timeSpent = 0L;
        this.timeToMake = 0L;
        this.orderId = 0L;
    }

    public void reset(){
        lastUpdateTime = 0L;
        timeSpent = 0L;
        timeToMake = 0L;
        idle = true;
        orderId = 0L;
    }

    public OrderStatus takeOrder(SushiOrder order, CachedService cachedService, SushiOrderRepository orderRepository){
        orderId = order.getId();
        timeToMake = cachedService.getSushiById(order.getSushiId()).getTimeToMake() * 1000L;
        timeSpent = 0L;
        idle = false;
        lastUpdateTime = System.currentTimeMillis();
        // set order in-progress
        if(!order.getStatusId().equals(cachedService.getStatusIdByName(Constant.STATUS_IN_PROGRESS))){
            order.setStatusId(cachedService.getStatusIdByName(Constant.STATUS_IN_PROGRESS));
            orderRepository.save(order);
        }
        return new OrderStatus(order.getId(), timeSpent, cachedService.getStatusNameById(order.getStatusId()), lastUpdateTime);
    }

    public OrderStatus resumeOrder(SushiOrder order, OrderStatus prevOrderStatus, CachedService cachedService, SushiOrderRepository orderRepository){
        orderId = order.getId();
        timeToMake = cachedService.getSushiById(order.getSushiId()).getTimeToMake() * 1000L;
        timeSpent = prevOrderStatus.getTimeSpent();
        idle = false;
        lastUpdateTime = System.currentTimeMillis();
        
        return new OrderStatus(order.getId(), timeSpent, cachedService.getStatusNameById(order.getStatusId()), lastUpdateTime);
    }

    public OrderStatus processOrder(CachedService cachedService, SushiOrderRepository orderRepository){
        Long currentTime = System.currentTimeMillis();
        timeSpent = timeSpent + (currentTime - lastUpdateTime);
        lastUpdateTime = currentTime;

        // refresh order status from database
        SushiOrder order = orderRepository.findById(orderId).get();
        if(timeSpent >= timeToMake){
            // finished order
            timeSpent = timeToMake;
            order.setStatusId(cachedService.getStatusIdByName(Constant.STATUS_FINISHED));
            orderRepository.save(order);
        }
        String status = cachedService.getStatusNameById(order.getStatusId());
        OrderStatus orderStatus = new OrderStatus(order.getId(), timeSpent, status, lastUpdateTime);
        // if orders status is cancelled, then do release the chef for next order
        // check order status to ensure it is not puased
        if(Constant.STATUS_CANCELLED.equals(status) || Constant.STATUS_FINISHED.equals(status) || Constant.STATUS_PAUSED.equals(status)){
            reset();
        }

        return orderStatus;
    }

    public boolean isIdle() {
        return idle;
    }

    

    public void setIdle(boolean idle) {
        this.idle = idle;
    }

    public Long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Long getTimeToMake() {
        return timeToMake;
    }

    public void setTimeToMake(Long timeToMake) {
        this.timeToMake = timeToMake;
    }

    public Long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(Long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Integer getChefId() {
        return chefId;
    }

    public void setChefId(Integer chefId) {
        this.chefId = chefId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((chefId == null) ? 0 : chefId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Chef other = (Chef) obj;
        if (chefId == null) {
            if (other.chefId != null)
                return false;
        } else if (!chefId.equals(other.chefId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Chef [chefId=" + chefId + ", idle=" + idle + ", lastUpdateTime=" + lastUpdateTime + ", timeToMake="
                + timeToMake + ", timeSpent=" + timeSpent + ", orderId=" + orderId + "]";
    }

    
}
