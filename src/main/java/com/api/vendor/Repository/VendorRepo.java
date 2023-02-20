package com.api.vendor.Repository;

import com.api.vendor.Model.Temp.TempVendor;
import com.api.vendor.Model.Vendor;
import com.api.vendor.Model.VendorDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepo extends JpaRepository<Vendor, String> {
    @Query(value = "SELECT u FROM Vendor u WHERE u.status =:status")
    List<Vendor> findByStatus(String status);

    @Query(value = "SELECT u FROM Vendor u WHERE u.orgEmail=:orgEmail")
    Optional<TempVendor> findByEmail(String orgEmail);

    @Query(value = "SELECT u FROM VendorDocuments u WHERE u.vendor.vendorId =:id")
    List<VendorDocuments> findByVendorId(String id);
}
