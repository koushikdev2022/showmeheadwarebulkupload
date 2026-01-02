package alison.customeheadware.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import alison.customeheadware.entity.HatImage;

import java.util.List;
import java.util.Optional;

public interface HatImageRepository extends JpaRepository<HatImage, Long> {
    
   
}

