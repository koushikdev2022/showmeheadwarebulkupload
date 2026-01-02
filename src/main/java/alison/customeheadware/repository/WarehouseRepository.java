package alison.customeheadware.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import alison.customeheadware.entity.Warehouse;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    
    // Derived query methods
    List<Warehouse> findByIsActive(Integer isActive);
    
    Optional<Warehouse> findByCode(String code);
    
    Optional<Warehouse> findByName(String name);
    
    List<Warehouse> findByNameContainingIgnoreCase(String name);
    
    // Custom queries
    @Query("SELECT w FROM Warehouse w WHERE w.isActive = 1 ORDER BY w.name ASC")
    List<Warehouse> findAllActiveWarehouses();
    
    @Query("SELECT w FROM Warehouse w WHERE w.code = :code AND w.isActive = 1")
    Optional<Warehouse> findActiveWarehouseByCode(@Param("code") String code);
    
    boolean existsByCode(String code);
}

