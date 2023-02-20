package com.api.vendor.Repository;

import com.api.vendor.Model.Temp.TempVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TempVendorRepo extends JpaRepository<TempVendor, String> {
    @Query(value = "SELECT u.vendorId FROM TempVendor u")
    List<String> findAllId();

    @Query(value = "SELECT u FROM TempVendor u WHERE u.orgEmail=:orgEmail")
    Optional<TempVendor> findByEmail(String orgEmail);

    @Query(value = "SELECT u FROM TempVendor u WHERE u.approvalStatus =:status")
    List<TempVendor> findByStatus(String status);

    @Query(value = "SELECT u FROM TempVendor u WHERE u.approvalStatus ='DECLINED' AND u.status='DECLINED'")
    List<TempVendor> findByDeclined();

    @Query(value = "SELECT u FROM TempVendor u WHERE u.status ='BLACKLIST'")
    List<TempVendor> findBlackList();
}
