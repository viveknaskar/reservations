package com.viveknaskar.landon.data.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.viveknaskar.landon.data.entity.Guest;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestRepository extends PagingAndSortingRepository<Guest, Long> {

}