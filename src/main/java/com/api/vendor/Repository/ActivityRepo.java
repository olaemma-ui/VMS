package com.api.vendor.Repository;

import com.api.vendor.Model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityRepo extends JpaRepository<ActivityLog, String> {
}
