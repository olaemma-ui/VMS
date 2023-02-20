package com.api.vendor.Repository;

import com.api.vendor.Model.VendorDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorDocsRepo extends JpaRepository<VendorDocuments, String> {
}
