package com.api.vendor.Repository;

import com.api.vendor.Model.Temp.TempDocs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempDocsRepo extends JpaRepository<TempDocs, String> {
}
