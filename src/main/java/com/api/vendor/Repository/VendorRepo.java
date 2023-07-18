package com.api.vendor.Repository;

import com.api.vendor.Model.Temp.TempVendor;
import com.api.vendor.Model.Vendor;
import com.api.vendor.Model.VendorDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepo extends JpaRepository<Vendor, String> {
    @Transactional
    @Query(value = "SELECT u FROM Vendor u WHERE u.status =:status")
    List<Vendor> findByStatus(String status);

    @Transactional
    @Query(value = "SELECT u FROM Vendor u WHERE u.orgEmail=:orgEmail AND u.vendorId <>:vendorId")
    Optional<Vendor> findByEmail(String orgEmail, String vendorId);

    @Transactional
    @Query(value = "SELECT u FROM VendorDocuments u WHERE u.vendor.vendorId =:id")
    List<VendorDocuments> findByVendorId(String id);
}
