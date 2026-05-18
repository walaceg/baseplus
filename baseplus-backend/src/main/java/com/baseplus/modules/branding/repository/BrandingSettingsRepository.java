package com.baseplus.modules.branding.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.baseplus.modules.branding.domain.BrandingSettings;

public interface BrandingSettingsRepository extends JpaRepository<BrandingSettings, Long> {

    Optional<BrandingSettings> findFirstByOrderByIdAsc();

    List<BrandingSettings> findAllByOrderByIdAsc();

    @Modifying
    @Query("delete from BrandingSettings b where b.id <> :keepId")
    void deleteAllExcept(@Param("keepId") Long keepId);
}
