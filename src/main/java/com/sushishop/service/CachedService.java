package com.sushishop.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;
import com.sushishop.entity.Status;
import com.sushishop.entity.Sushi;
import com.sushishop.repository.StatusRepository;
import com.sushishop.repository.SushiRepository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;

@Service
public class CachedService {
 
    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private SushiRepository sushiRepository;

    @PostConstruct
    public void init() {
        // init the statuses
        Map<String, Integer> statuses = hazelcastInstance.getMap("statuses");
        List<Status> statusList = statusRepository.findAll();
        for(Status status : statusList){
            statuses.put(status.getName(), status.getId());
        }
        // init the sushi type
        List<Sushi> sushies = hazelcastInstance.getList("sushies");
        if(sushies.size() == 0){
            List<Sushi> sushiList = sushiRepository.findAll();
            for(Sushi sushi : sushiList){
                sushies.add(sushi);
            }
        }
    }

    public Map<String, Integer> getStatuses(){
        
        return hazelcastInstance.getMap("statuses");
    }

    public List<Sushi> getSushies(){
        return hazelcastInstance.getList("sushies");
    }

    public Sushi getSushiById(Long id){
        for(Sushi sushi : this.getSushies()){
            if(sushi.getId().equals(id)){
                return sushi;
            }
        }
        // not found
        throw new EntityNotFoundException("Sushi not found. ID:" + id);
    }

    public Integer getStatusIdByName(String name){
        
        Integer statusId = getStatuses().get(name);
        if(statusId == null){
            throw new EntityNotFoundException("Status not found. Name:" + name);
        }
        return statusId;
    }

    public String getStatusNameById(Integer id){
        for(Map.Entry<String, Integer> entry : this.getStatuses().entrySet()){
            if(entry.getValue().equals(id)){
                return entry.getKey();
            }
        }
        // not found
        throw new EntityNotFoundException("Status not found. ID:" + id);
    }
    
}
