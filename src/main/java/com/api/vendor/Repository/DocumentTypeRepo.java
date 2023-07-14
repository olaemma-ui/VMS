package com.api.vendor.Repository;

import com.api.vendor.Model.DocumentTypeTable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DocumentTypeRepo extends JpaRepository<DocumentTypeTable, String> {

}
