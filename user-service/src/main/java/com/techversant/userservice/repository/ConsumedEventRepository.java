package com.techversant.userservice.repository;


import com.techversant.userservice.model.ConsumedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumedEventRepository extends JpaRepository<ConsumedEvent, String> {
    boolean existsByEventId(String eventId);
}
