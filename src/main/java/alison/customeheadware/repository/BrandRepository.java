package alison.customeheadware.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import alison.customeheadware.entity.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    // Derived query methods
    List<Brand> findByIsActive(Integer isActive);
    
    Optional<Brand> findByCode(String code);
    
    Optional<Brand> findByName(String name);
    
    List<Brand> findByNameContainingIgnoreCase(String name);
    
    // Custom queries
    @Query("SELECT b FROM Brand b WHERE b.isActive = 1 ORDER BY b.name ASC")
    List<Brand> findAllActiveBrands();
    
    @Query("SELECT b FROM Brand b WHERE b.code = :code AND b.isActive = 1")
    Optional<Brand> findActiveBrandByCode(@Param("code") String code);
}

